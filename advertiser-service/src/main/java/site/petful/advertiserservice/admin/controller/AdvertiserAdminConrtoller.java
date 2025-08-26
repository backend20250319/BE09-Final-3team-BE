package site.petful.advertiserservice.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.petful.advertiserservice.admin.service.AdvertiserAdminService;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.entity.Advertiser;

@RestController
@RequestMapping("/admin/advertisers")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdvertiserAdminConrtoller {
    private final AdvertiserAdminService advertiserAdminService;

    @PostMapping("/{id}/restrict")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> restrict(
            @PathVariable("id") Long id
    ){
        advertiserAdminService.restrictAdvertiser(id);
        return ApiResponseGenerator.success();
    }

    @GetMapping("/all")
    public ApiResponse<Page<Advertiser>> getAll(
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return ApiResponseGenerator.success(advertiserAdminService.getAllAdvertiser(pageable));
    }


    @PatchMapping("/{id}/reject")
    public ApiResponse<Void> reject(
            @PathVariable("id") Long advertiserId,
            @RequestBody String reason
    ) {
        advertiserAdminService.rejectAdvertiser(advertiserId, reason);
        return ApiResponseGenerator.success();
    }
    @PatchMapping("/{id}/approve")
    public ApiResponse<Void> approve(
            @PathVariable("id") Long advertiserId
    ){
        advertiserAdminService.approveAdvertiser(advertiserId);
        return ApiResponseGenerator.success();
    }
}
