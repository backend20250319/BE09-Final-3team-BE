package site.petful.campaignservice.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import site.petful.campaignservice.admin.service.AdvertisementAdminService;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.common.ApiResponseGenerator;
import site.petful.campaignservice.dto.advertisement.AdsResponse;

@RestController
@RequestMapping("/admin/campaign")
@RequiredArgsConstructor
public class AdvertisementAdminController {
    private final AdvertisementAdminService campaignAdminService;

    @GetMapping("/trial")
    public ApiResponse<Page<AdsResponse>> getAllCampaigns(
            @PageableDefault(size = 5, sort = "campaignStart", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return ApiResponseGenerator.success(campaignAdminService.getAllCampaign(pageable));
    }

    @PatchMapping("/{id}/delete")
    public ApiResponse deleteCampaign(
            @PathVariable Long adId
    ){
        campaignAdminService.deleteCampaign(adId);
        return ApiResponseGenerator.success();
    }

    @GetMapping("/pending")
    public ApiResponse<Page<AdsResponse>> getPendingCampaigns(
            @PageableDefault(size = 5, sort = "campaignStart", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return ApiResponseGenerator.success(campaignAdminService.getPendingAds(pageable));
    }

    @PatchMapping("{id}/approve")
    public ApiResponse<Void> approveCampaign(
            @PathVariable Long adId
    ){
        campaignAdminService.approve(adId);
        return ApiResponseGenerator.success();
    }

    @PatchMapping("{id}/reject")
    public ApiResponse<Void> rejectCampaign(
            @PathVariable Long adId,
            @RequestParam String reason
            ){
        campaignAdminService.reject(adId,reason);
        return ApiResponseGenerator.success();
    }
}
