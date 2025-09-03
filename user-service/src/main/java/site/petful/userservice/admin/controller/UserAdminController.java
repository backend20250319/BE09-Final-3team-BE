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
import site.petful.userservice.admin.dto.UserResponse;
import site.petful.userservice.admin.entity.ActorType;
import site.petful.userservice.admin.entity.ReportStatus;
import site.petful.userservice.admin.service.AdminAuthService;
import site.petful.userservice.admin.service.UserAdminService;
import site.petful.userservice.common.ApiResponse;
import site.petful.userservice.common.ApiResponseGenerator;
import site.petful.userservice.entity.User;
import site.petful.userservice.common.ErrorCode;
import jakarta.validation.Valid;

import java.util.Map;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('Admin')")
public class UserAdminController {
    private final UserAdminService userAdminService;
    private final AdminAuthService adminAuthService;

    @GetMapping("/restrict/all")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getReportUsers(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) ActorType targetType,
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        try {
            log.info("getReportUsers API 호출됨 - user: {}", user);

            Long userNo = user != null ? user.getUserNo() : null;
            String userType = user != null ? user.getUserType().name() : null;

            log.info("사용자 정보 - userNo: {}, userType: {}", userNo, userType);

            Page<ReportResponse> page = userAdminService.getAllReports(userNo, userType, targetType, status, pageable);
            log.info("신고 목록 조회 성공 - 총 {}개", page.getTotalElements());
            return ResponseEntity.ok(ApiResponseGenerator.success(page));
        } catch (Exception e) {
            log.error("getReportUsers API 오류: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PatchMapping("/restrict/{reportId}")
    public ResponseEntity<ApiResponse<Void>> restrictReportUser(
            @AuthenticationPrincipal User user,
            @PathVariable Long reportId
    ) {
        log.info("restrictReportUser API 호출됨 - reportId: {}", reportId);
        userAdminService.restrictByReport(reportId);
        return ResponseEntity.ok(ApiResponseGenerator.success());
    }

    @PatchMapping("/restrict/{reportId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectReport(
            @AuthenticationPrincipal User user,
            @PathVariable Long reportId,
            @RequestBody(required = false) Map<String, String> requestBody
    ) {
        String reason = requestBody != null ? requestBody.get("reason") : null;
        log.info("rejectReport API 호출됨 - reportId: {}, reason: {}", reportId, reason);
        userAdminService.rejectByReport(reportId, reason);
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        try {
            log.info("getUserById API 호출됨 - user: {}, id: {}", user, id);
            UserResponse userResponse = userAdminService.getUserById(id);
            log.info("사용자 조회 성공 - id: {}, name: {}", id, userResponse.getName());
            return ResponseEntity.ok(ApiResponseGenerator.success(userResponse));
        } catch (Exception e) {
            log.error("getUserById API 오류: {}", e.getMessage(), e);
            throw e;
        }
    }

}
