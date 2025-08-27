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
import site.petful.advertiserservice.dto.advertisement.AdResponse;
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


    @GetMapping("/trial")
    public ApiResponse<Page<AdResponse>> getAllCampaigns(
            @PageableDefault(size = 5, sort = "campaignStart", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        Page<AdResponse> dtoPage = advertiserAdminService.getAllCampaign(pageable)
                .map(AdResponse::from);
        return ApiResponseGenerator.success(dtoPage);
    }

    @PatchMapping("/{id}/delete")
    public ApiResponse deleteCampaign(
            @PathVariable Long adId
    ){
        advertiserAdminService.deleteCampaign(adId);
        return ApiResponseGenerator.success();
    }

    @GetMapping("/pending")
    public ApiResponse<Page<AdResponse>> getPendingCampaigns(
            @PageableDefault(size = 5, sort = "campaignStart", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        Page<AdResponse> dtoPage = advertiserAdminService.getAllCampaign(pageable)
                .map(AdResponse::from);
        return ApiResponseGenerator.success(dtoPage);
    }

    @PatchMapping("{id}/approve")
    public ApiResponse<Void> approveCampaign(
            @PathVariable Long adId
    ){
        advertiserAdminService.approve(adId);
        return ApiResponseGenerator.success();
    }

    @PatchMapping("{id}/reject")
    public ApiResponse<Void> rejectCampaign(
            @PathVariable Long adId,
            @RequestParam String reason
    ){
        advertiserAdminService.reject(adId,reason);
        return ApiResponseGenerator.success();
    }


}
