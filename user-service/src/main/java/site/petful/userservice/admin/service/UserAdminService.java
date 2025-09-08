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
import site.petful.userservice.admin.dto.UserResponse;
import site.petful.userservice.admin.entity.ActorRef;
import site.petful.userservice.admin.entity.ActorType;
import site.petful.userservice.admin.entity.ReportLog;
import site.petful.userservice.admin.entity.ReportStatus;
import site.petful.userservice.admin.repository.ReportLogRepository;
import site.petful.userservice.common.ApiResponse;
import site.petful.userservice.common.ApiResponseGenerator;
import site.petful.userservice.common.ErrorCode;
import site.petful.userservice.entity.User;
import site.petful.userservice.entity.UserProfile;
import site.petful.userservice.repository.UserRepository;
import site.petful.userservice.repository.UserProfileRepository;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ReportLogRepository reportLogRepository;
    private final AdvertiserClient advertiserClient;

    public Page<ReportResponse> getAllReports(Long adminId, String adminType, ActorType targetType, ReportStatus status, Pageable pageable) {
        log.info("getAllReports 호출됨 - adminId: {}, adminType: {}, targetType: {}, status: {}", 
                adminId, adminType, targetType, status);
        
        Page<ReportLog> logs;

        // 401: 인증 실패
        if (adminId == null) {
            log.error("adminId가 null입니다.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."); // 401
        }

        // 403: 인가 실패 (관리자가 아닌 경우)
        if (!"ADMIN".equalsIgnoreCase(adminType)) {
            log.error("관리자가 아닙니다. adminType: {}", adminType);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 접근할 수 있습니다."); // 403
        }

        try {
            // 기본적으로 BEFORE 상태만 조회
            ReportStatus defaultStatus = ReportStatus.BEFORE;
            
            // 먼저 기본 조건으로 모든 데이터를 조회
            if(targetType != null && status != null) {
                log.info("targetType과 status로 조회: {}, {}", targetType, status);
                logs = reportLogRepository.findByTarget_TypeAndReportStatusOrderByCreatedAtDesc(targetType, status, pageable);
            } else if(targetType != null) {
                log.info("targetType으로 조회 (BEFORE만): {}", targetType);
                logs = reportLogRepository.findByTarget_TypeAndReportStatusOrderByCreatedAtDesc(targetType, defaultStatus, pageable);
            } else if(status != null) {
                log.info("status로 조회: {}", status);
                logs = reportLogRepository.findByReportStatusOrderByCreatedAtDesc(status, pageable);
            } else{
                log.info("BEFORE 상태만 조회");
                logs = reportLogRepository.findByReportStatusOrderByCreatedAtDesc(defaultStatus, pageable);
            }
            
            
            log.info("조회된 신고 수: {}", logs.getTotalElements());
            return logs.map(this::toDto);
        } catch (Exception e) {
            log.error("신고 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "신고 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional
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

    @Transactional
    public void rejectByReport(Long reportId, String reason) {
        ReportLog log = reportLogRepository.findById(reportId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 신고 ID: " + reportId)
                );
        log.setReportStatus(ReportStatus.REJECTED);
        if (reason != null && !reason.trim().isEmpty()) {
            log.setRejectReason(reason);
        }
    }

    public UserResponse getUserById(Long userId) {
        log.info("getUserById 호출됨 - userId: {}", userId);
        try {
            User user = userRepository.findById(userId).orElse(null);
            
            if (user == null) {
                log.warn("사용자를 찾을 수 없음 - userId: {}", userId);
                // 사용자를 찾을 수 없는 경우 기본값 반환
                return new UserResponse(
                        userId,
                        "알 수 없음",
                        "알 수 없음",
                        "알 수 없음"
                );
            }
            
            log.info("사용자 조회 성공 - userId: {}, name: {}, phone: {}, email: {}", 
                    userId, user.getName(), user.getPhone(), user.getEmail());
            
            // 사용자 정보가 null인 경우 기본값으로 처리
            String userName = user.getName() != null ? user.getName() : "이름 없음";
            String userPhone = user.getPhone() != null ? user.getPhone() : "전화번호 없음";
            String userEmail = user.getEmail() != null ? user.getEmail() : "이메일 없음";
            
            return new UserResponse(
                    user.getUserNo(),
                    userName,
                    userPhone,
                    userEmail
            );
        } catch (Exception e) {
            log.error("getUserById 예상치 못한 오류 - userId: {}, error: {}", userId, e.getMessage(), e);
            // 예외 발생 시에도 기본값 반환
            return new UserResponse(
                    userId,
                    "알 수 없음",
                    "알 수 없음",
                    "알 수 없음"
            );
        }
    }

    private ReportResponse toDto(ReportLog e) {
        // 신고자 정보 조회
        String reporterNickname = "알 수 없음";
        String reporterProfileImage = null;
        if (e.getReporter().getType() == ActorType.USER) {
            try {
                User reporter = userRepository.findById(e.getReporter().getId()).orElse(null);
                if (reporter != null) {
                    reporterNickname = reporter.getNickname() != null ? reporter.getNickname() : reporter.getName();
                    // 프로필 이미지 조회
                    try {
                        UserProfile reporterProfile = userProfileRepository.findByUser_UserNo(e.getReporter().getId()).orElse(null);
                        if (reporterProfile != null) {
                            reporterProfileImage = reporterProfile.getProfileImageUrl();
                        }
                    } catch (Exception profileEx) {
                        log.warn("신고자 프로필 이미지 조회 실패: {}", profileEx.getMessage());
                    }
                }
            } catch (Exception ex) {
                log.warn("신고자 정보 조회 실패: {}", ex.getMessage());
            }
        }
        
        // 신고 대상 정보 조회
        String targetNickname = "알 수 없음";
        String targetProfileImage = null;
        if (e.getTarget().getType() == ActorType.USER) {
            try {
                User target = userRepository.findById(e.getTarget().getId()).orElse(null);
                if (target != null) {
                    targetNickname = target.getNickname() != null ? target.getNickname() : target.getName();
                    // 프로필 이미지 조회
                    try {
                        UserProfile targetProfile = userProfileRepository.findByUser_UserNo(e.getTarget().getId()).orElse(null);
                        if (targetProfile != null) {
                            targetProfileImage = targetProfile.getProfileImageUrl();
                        }
                    } catch (Exception profileEx) {
                        log.warn("신고 대상 프로필 이미지 조회 실패: {}", profileEx.getMessage());
                    }
                }
            } catch (Exception ex) {
                log.warn("신고 대상 정보 조회 실패: {}", ex.getMessage());
            }
        }
        
        return new ReportResponse(
                e.getId(),
                e.getReporter().getType(),
                e.getReporter().getId(),
                reporterNickname,
                reporterProfileImage,
                e.getTarget().getType(),
                e.getTarget().getId(),
                targetNickname,
                targetProfileImage,
                e.getReason(),
                e.getReportStatus(),
                e.getCreatedAt()
        );
    }
}
