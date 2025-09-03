package site.petful.userservice.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.petful.userservice.admin.dto.AdminLogoutRequest;
import site.petful.userservice.admin.dto.AdminLogoutResponse;
import site.petful.userservice.admin.dto.ReportResponse;
import site.petful.userservice.admin.entity.ActorType;
import site.petful.userservice.admin.entity.ReportStatus;
import site.petful.userservice.admin.service.AdminAuthService;
import site.petful.userservice.admin.service.UserAdminService;
import site.petful.userservice.common.ApiResponse;
import site.petful.userservice.common.ApiResponseGenerator;
import site.petful.userservice.common.ErrorCode;
import jakarta.validation.Valid;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {
    private final UserAdminService userAdminService;
    private final AdminAuthService adminAuthService;

    @GetMapping("/restrict/all")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getReportUsers(
            @AuthenticationPrincipal Long adminId,
            @AuthenticationPrincipal String adminType,
            @RequestParam(required = false) ActorType targetType,
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<ReportResponse> page = userAdminService.getAllReports(adminId,adminType,targetType, status, pageable);
        return ResponseEntity.ok(ApiResponseGenerator.success(page));
    }

    @PatchMapping("/restrict/{reportId}")
    public ResponseEntity<ApiResponse<Void>> restrictReportUser(
            @AuthenticationPrincipal Long adminId,
            @AuthenticationPrincipal String adminType,
            @PathVariable Long reportId
    ) {
        userAdminService.restrictByReport(reportId);
        return ResponseEntity.ok(ApiResponseGenerator.success());
    }

    @PatchMapping("/restrict/{reportId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectReport(
            @AuthenticationPrincipal Long adminId,
            @AuthenticationPrincipal String adminType,
            @PathVariable Long reportId
    ) {
        userAdminService.rejectByReport(reportId);
        return ResponseEntity.ok(ApiResponseGenerator.success());
    }

    /**
     * Admin 로그아웃
     * POST /api/v1/admin/users/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<AdminLogoutResponse>> logout(
            @AuthenticationPrincipal Long adminId,
            @AuthenticationPrincipal String adminType,
            @Valid @RequestBody AdminLogoutRequest request
    ) {
        try {
            // 리프레시 토큰 검증 및 로그아웃 처리
            adminAuthService.logout(request.getRefreshToken());
            
            AdminLogoutResponse response = AdminLogoutResponse.builder()
                    .message("Admin 로그아웃이 성공적으로 처리되었습니다.")
                    .adminId(adminId)
                    .adminType(adminType)
                    .build();
            
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(ErrorCode.INVALID_REQUEST, e.getMessage(), null));
        }
    }

}
