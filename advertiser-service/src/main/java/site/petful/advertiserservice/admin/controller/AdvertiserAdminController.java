package site.petful.advertiserservice.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.petful.advertiserservice.admin.service.AdvertiserAdminService;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.dto.advertisement.AdAdminResponse;
import site.petful.advertiserservice.dto.advertisement.AdResponse;
import site.petful.advertiserservice.dto.advertiser.AdvertiserAdminResponse;
import site.petful.advertiserservice.admin.dto.AdvertiserWithFilesResponse;
import site.petful.advertiserservice.dto.advertiser.AdvertiserResponse;
import site.petful.advertiserservice.entity.advertiser.Advertiser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN')")
public class AdvertiserAdminController {
    private final AdvertiserAdminService advertiserAdminService;

    @PostMapping("/advertiser/{id}/restrict")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> restrict(
            @AuthenticationPrincipal Long userNo,
            @PathVariable Long id
    ){
        advertiserAdminService.restrictAdvertiser(id);
        return ApiResponseGenerator.success();
    }

    @GetMapping("/advertiser/all")
    public ApiResponse<Page<AdvertiserWithFilesResponse>> getAll(
            @AuthenticationPrincipal Long userNo,
            @PageableDefault(size = 4, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        log.info("🔍 [AdvertiserAdminController] 미승인 광고주 목록 조회 요청");
        Page<AdvertiserWithFilesResponse> result = advertiserAdminService.getAllAdvertiser(pageable);
        log.info("✅ [AdvertiserAdminController] 미승인 광고주 목록 조회 성공: 총 {}개", result.getTotalElements());
        return ApiResponseGenerator.success(result);
    }


    @PatchMapping("/advertiser/{id}/reject")
    public ApiResponse<Void> reject(
            @AuthenticationPrincipal Long userNo,
            @PathVariable Long id,
            @RequestBody String reason
    ) {
        advertiserAdminService.rejectAdvertiser(id, reason);
        return ApiResponseGenerator.success();
    }
    @PatchMapping("/advertiser/{id}/approve")
    public ApiResponse<Void> approve(
            @AuthenticationPrincipal Long userNo,
            @PathVariable Long id
    ){
        advertiserAdminService.approveAdvertiser(id);
        return ApiResponseGenerator.success();
    }


    @GetMapping("/ad/trial")
    public ApiResponse<Page<AdAdminResponse>> getAllCampaigns(
            @AuthenticationPrincipal Long userNo,
            @PageableDefault(size = 4, sort = "campaignStart", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return ApiResponseGenerator.success(advertiserAdminService.getAllCampaign(pageable));
    }

    @PatchMapping("/ad/{adId}/delete")
    public ApiResponse<Void> deleteCampaign(
            @AuthenticationPrincipal Long userNo,
            @PathVariable Long adId
    ){
        advertiserAdminService.deleteCampaign(adId);
        return ApiResponseGenerator.success();
    }

    @GetMapping("/ad/pending")
    public ApiResponse<Page<AdAdminResponse>> getPendingCampaigns(
            @AuthenticationPrincipal Long userNo,
            @PageableDefault(size = 4, sort = "campaignStart", direction = Sort.Direction.DESC)
            Pageable pageable
    ){

        return ApiResponseGenerator.success(advertiserAdminService.getPendingAds(pageable));
    }

    @PatchMapping("/ad/{adId}/approve")
    public ApiResponse<Void> approveCampaign(
            @AuthenticationPrincipal Long userNo,
            @PathVariable Long adId
    ){
        advertiserAdminService.approve(adId);
        return ApiResponseGenerator.success();
    }

    @PatchMapping("/ad/{adId}/reject")
    public ApiResponse<Void> rejectCampaign(
            @AuthenticationPrincipal Long userNo,
            @PathVariable Long adId,
            @RequestParam String reason
    ){
        advertiserAdminService.reject(adId,reason);
        return ApiResponseGenerator.success();
    }


}
