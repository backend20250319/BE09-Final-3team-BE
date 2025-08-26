package site.petful.userservice.admin.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.userservice.admin.client.AdvertiserClient;
import site.petful.userservice.admin.dto.PetStarResponse;
import site.petful.userservice.admin.dto.ReportResponse;
import site.petful.userservice.admin.entity.ActorRef;
import site.petful.userservice.admin.entity.ActorType;
import site.petful.userservice.admin.entity.ReportLog;
import site.petful.userservice.admin.entity.ReportStatus;
import site.petful.userservice.admin.repository.ReportLogRepository;
import site.petful.userservice.entity.Pet;
import site.petful.userservice.entity.PetStarStatus;
import site.petful.userservice.entity.User;
import site.petful.userservice.repository.PetRepository;
import site.petful.userservice.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminService {
    private final UserRepository userRepository;
    private final ReportLogRepository reportLogRepository;
    private final PetRepository petRepository;
    private final AdvertiserClient advertiserClient;

    public Page<ReportResponse> getAllReports(ActorType targetType, ReportStatus status, Pageable pageable) {
        Page<ReportLog> logs;

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
                .orElseThrow(() -> new IllegalArgumentException("report not found: " + reportId));

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
                .orElseThrow(() -> new IllegalArgumentException("report not found: " + reportId));
        log.setReportStatus(ReportStatus.REJECTED);
    }

    public Page<PetStarResponse> getAllPetStars(Pageable pageable) {
        Page<Pet> page = petRepository.findByPetStarStatus(PetStarStatus.PENDING, pageable);

        List<Long> userNos = page.getContent().stream()
                .map(Pet::getUserNo)
                .distinct()
                .toList();

        Map<Long,User> userMap = StreamSupport
                .stream(userRepository.findAllById(userNos).spliterator(), false)
                .collect(Collectors.toMap(User::getUserNo, Function.identity()));

        List<PetStarResponse> rows = page.getContent().stream()
                .map(p -> {
                    User u =userMap.get(p.getUserNo());
                    return new PetStarResponse(
                            p.getSnsProfileNo(),
                            p.getName(),
                            u != null ? u.getUsername() : null,
                            u != null ? u.getPhone() : null,
                            u != null ? u.getEmail() : null
                    );
                }).toList();

        return new PageImpl<>(rows, pageable, page.getTotalElements());
    }

    public void approvePetStar(Long petStarNo) {
        petRepository.findById(petStarNo)
                .ifPresent(p -> p.setPetStarStatus(PetStarStatus.ACTIVE));
    }

    public void rejectPetStar(Long petStarNo) {
        petRepository.findById(petStarNo)
                .ifPresent(p -> p.setPetStarStatus(PetStarStatus.REJECTED));
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
