package site.petful.userservice.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.petful.userservice.admin.dto.PetStarResponse;
import site.petful.userservice.admin.dto.ReportResponse;
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
    public ApiResponse<Page<ReportResponse>> getReportUsers(
            @RequestHeader("X-User-No") Long userNo,
            @RequestHeader("X-User-Type")String userType,
            @PageableDefault(
                    size = 5,
                    sort = "createdAt",
                    direction =Sort.Direction.DESC
    ) Pageable pageable
    ) {
        return ApiResponseGenerator.success(userAdminService.getAllReportUsers(pageable));
    }
    @PatchMapping("/restrict/{targetId}")
    public ApiResponse<Void> restrictReportUser(
            @RequestHeader("X-User-No") Long userNo,
            @RequestHeader("X-User-Type")String userType,
            @RequestParam Long reporterId,
            @PathVariable Long targetId
            ){
            userAdminService.restrictReporterUser(reporterId,targetId);
            return ApiResponseGenerator.success();
    }

    @PatchMapping("/restrict/{targetId}/reject")
    public ApiResponse<Void> rejectReport(
            @RequestHeader("X-User-No") Long userNo,
            @RequestHeader("X-User-Type")String userType,
            @RequestParam Long reporterId,
            @PathVariable Long targetId
    ){
        userAdminService.rejectReport(reporterId,targetId);
        return ApiResponseGenerator.success();
    }

    @GetMapping("/petstar/all")
    public ApiResponse<Page<PetStarResponse>> getAllPetStars(
            @RequestHeader("X-User-No") Long userNo,
            @RequestHeader("X-User-Type")String userType,
            @PageableDefault(
                    size = 5,
                    sort = "pendingAt",
                    direction =Sort.Direction.DESC
            ) Pageable pageable
    ){
        return ApiResponseGenerator.success(userAdminService.getAllPetStars(pageable));
    }

    @PatchMapping("/petstar/{id}/approve")
    public ApiResponse<Void> approvePetStar(
            @RequestHeader("X-User-No") Long userNo,
            @RequestHeader("X-User-Type")String userType,
            @RequestParam Long petStarNo
    ){
        userAdminService.approvePetStar(petStarNo);
            return ApiResponseGenerator.success();
    }

    @PatchMapping("/petstar/{id}/reject")
    public ApiResponse<Void> rejectPetStar(
                    @RequestHeader("X-User-No") Long userNo,
                    @RequestHeader("X-User-Type")String userType,
                    @RequestParam Long petStarNo
            ){
            userAdminService.rejectPetStar(petStarNo);
            return ApiResponseGenerator.success();
    }
}
