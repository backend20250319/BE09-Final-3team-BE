package site.petful.petservice.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.petful.petservice.admin.service.PetAdminService;
import site.petful.petservice.common.ApiResponse;
import site.petful.petservice.dto.PetStarResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/pet")
@PreAuthorize("hasRole('Admin')")
public class PetAdminController {
    private final PetAdminService petAdminService;

    // PetStar 목록 조회 (관리자용)
    @GetMapping("/applications")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<ApiResponse<Page<PetStarResponse>>> getPetStarApplications(
            @AuthenticationPrincipal Long userNo,
            @PageableDefault(size = 10, sort = "pendingAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        try {
            log.info("getPetStarApplications API 호출됨 - userNo: {}", userNo);
            Page<PetStarResponse> applications = petAdminService.getPetStarApplications(pageable);
            log.info("펫스타 신청 목록 조회 성공 - 총 {}개", applications.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(applications));
        } catch (Exception e) {
            log.error("getPetStarApplications API 오류: {}", e.getMessage(), e);
            throw e;
        }
    }

    // PetStar 승인 (관리자용)
    @PatchMapping("/{petNo}/approve")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<ApiResponse<Void>> approvePetStar(
            @AuthenticationPrincipal Long userNo,
            @PathVariable Long petNo) {
        log.info("approvePetStar API 호출됨 - userNo: {}, petNo: {}", userNo, petNo);
        petAdminService.approvePetStar(petNo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // PetStar 거절 (관리자용)
    @PatchMapping("/{petNo}/reject")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<ApiResponse<Void>> rejectPetStar(
            @AuthenticationPrincipal Long userNo,
            @RequestBody String reason,
            @PathVariable Long petNo) {
        log.info("rejectPetStar API 호출됨 - userNo: {}, petNo: {}", userNo, petNo,reason);
        petAdminService.rejectPetStar(petNo,reason);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
