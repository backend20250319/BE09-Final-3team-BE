package site.petful.userservice.admin.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.userservice.admin.dto.PetStarResponse;
import site.petful.userservice.admin.dto.ReportResponse;
import site.petful.userservice.admin.entity.ReportLog;
import site.petful.userservice.admin.entity.ReportStatus;
import site.petful.userservice.admin.repository.ReportLogRepository;
import site.petful.userservice.domain.Pet;
import site.petful.userservice.domain.PetStarStatus;
import site.petful.userservice.domain.User;
import site.petful.userservice.repository.PetRepository;
import site.petful.userservice.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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


    public Page<ReportResponse> getAllReportUsers(Pageable pageable) {
        return reportLogRepository.findByStatus(ReportStatus.BEFORE, pageable)
                .map(ReportResponse::from);
    }

    public void restrictReporterUser(Long reporterId, Long targetId) {
        ReportLog log = reportLogRepository.findTopByReporterNoAndTargetNoAndReportStatusOrderByCreatedAtDesc(
                reporterId,targetId,ReportStatus.BEFORE)
                .orElseThrow(()-> new IllegalStateException("이미 처리되었거나 존재하지 않는 신고입니다."));
        User target = userRepository.findById(targetId)
                .orElseThrow(()->new EntityNotFoundException("존재하지 않는 유저입니다."));

        if(Boolean.TRUE.equals((target.getIsActive()))){
            target.setIsActive(false);
        }
        log.setReportStatus(ReportStatus.AFTER);
        log.setCreatedAt(LocalDateTime.now());
    }


    public void rejectReport(Long reporterId, Long targetId) {
        reportLogRepository.findByReporterNoAndTargetNoAndReportStatus(reporterId,targetId,ReportStatus.BEFORE)
                .ifPresent(r -> r.setReportStatus(ReportStatus.REJECTED));
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

}
