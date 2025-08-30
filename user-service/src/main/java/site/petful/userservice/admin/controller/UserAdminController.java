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
import site.petful.userservice.admin.dto.ReportResponse;
import site.petful.userservice.admin.entity.ActorType;
import site.petful.userservice.admin.entity.ReportStatus;
import site.petful.userservice.admin.service.UserAdminService;
import site.petful.userservice.common.ApiResponse;
import site.petful.userservice.common.ApiResponseGenerator;


@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {
    private final UserAdminService userAdminService;

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

}
