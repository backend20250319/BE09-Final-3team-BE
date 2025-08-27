package org.example.petservice.admin.controller;

import lombok.RequiredArgsConstructor;
import org.example.petservice.admin.service.PetAdminService;
import org.example.petservice.common.ApiResponse;
import org.example.petservice.dto.PetStarResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/pet")
@PreAuthorize("hasRole('ADMIN')")
public class PetAdminController {
    private final PetAdminService petAdminService;

    // PetStar 목록 조회 (관리자용)
    @GetMapping("/admin/petstar/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PetStarResponse>>> getPetStarApplications(
            @PageableDefault(size = 10, sort = "pendingAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<PetStarResponse> applications = petAdminService.getPetStarApplications(pageable);
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    // PetStar 승인 (관리자용)
    @PatchMapping("/admin/petstar/{petNo}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> approvePetStar(@PathVariable Long petNo) {
        petAdminService.approvePetStar(petNo);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // PetStar 거절 (관리자용)
    @PatchMapping("/admin/petstar/{petNo}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> rejectPetStar(@PathVariable Long petNo) {
        petAdminService.rejectPetStar(petNo);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
