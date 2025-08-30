package site.petful.userservice.admin.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import site.petful.userservice.admin.client.AdvertiserClient;
import site.petful.userservice.admin.dto.ReportResponse;
import site.petful.userservice.admin.entity.ActorRef;
import site.petful.userservice.admin.entity.ActorType;
import site.petful.userservice.admin.entity.ReportLog;
import site.petful.userservice.admin.entity.ReportStatus;
import site.petful.userservice.admin.repository.ReportLogRepository;
import site.petful.userservice.common.ApiResponse;
import site.petful.userservice.common.ApiResponseGenerator;
import site.petful.userservice.common.ErrorCode;
import site.petful.userservice.entity.User;
import site.petful.userservice.repository.UserRepository;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminService {
    private final UserRepository userRepository;
    private final ReportLogRepository reportLogRepository;
    private final AdvertiserClient advertiserClient;

    public Page<ReportResponse> getAllReports(Long adminId, String adminType, ActorType targetType, ReportStatus status, Pageable pageable) {
        Page<ReportLog> logs;

        // 401: 인증 실패
        if (adminId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."); // 401
        }

        // 403: 인가 실패 (관리자가 아닌 경우)
        if (!"ADMIN".equalsIgnoreCase(adminType)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다."); // 403
        }

        if(targetType != null && status != null) {
            logs = reportLogRepository.findByTarget_TypeAndReportStatusOrderByCreatedAtDesc(targetType, status, pageable);
        } else if(targetType != null) {
            logs = reportLogRepository.findByTarget_TypeOrderByCreatedAtDesc(targetType,pageable);
        } else if(status != null) {
            logs = reportLogRepository.findByReportStatusOrderByCreatedAtDesc(status, pageable);
        } else{
            logs = reportLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return logs.map(this::toDto);
    }

    public void restrictByReport(Long reportId) {
        ReportLog log = reportLogRepository.findById(reportId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 신고 ID: " + reportId)
                );
        ActorRef target = log.getTarget();
        switch (target.getType()) {
            case USER  -> suspendUserLocally(target.getId());        // 유저 정지/제재
            case ADVERTISER -> advertiserClient.blacklistAdvertiser(target.getId()); // 광고주 블랙리스트/제재
            default -> throw new IllegalStateException("지원하지 않는 유저입니다." + target.getType());
        }
        log.setReportStatus(ReportStatus.AFTER);
        log.setCreatedAt(LocalDateTime.now());
    }

    private void suspendUserLocally(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다." + userId));
        user.suspend();
    }

    public void rejectByReport(Long reportId) {
        ReportLog log = reportLogRepository.findById(reportId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 신고 ID: " + reportId)
                );
        log.setReportStatus(ReportStatus.REJECTED);
    }

    private ReportResponse toDto(ReportLog e) {
        return new ReportResponse(
                e.getId(),
                e.getReporter().getType(),
                e.getReporter().getId(),
                e.getTarget().getType(),
                e.getTarget().getId(),
                e.getReason(),
                e.getReportStatus(),
                e.getCreatedAt()
        );
    }
}
