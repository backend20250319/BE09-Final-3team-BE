package site.petful.campaignservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.common.ApiResponseGenerator;
import site.petful.campaignservice.dto.advertisement.*;
import site.petful.campaignservice.security.SecurityUtil;
import site.petful.campaignservice.service.AdService;

import java.util.List;

@RestController
@RequestMapping("/ad")
public class AdController {

    private final AdService adService;
    private final SecurityUtil securityUtil;

    public AdController(AdService adService, SecurityUtil securityUtil) {
        this.adService = adService;
        this.securityUtil = securityUtil;
    }

    // 1. adStatus별(모집중/종료된) 광고(캠페인) 전체 조회
    @GetMapping("/adStatus")
    public ResponseEntity<ApiResponse<AdsGroupedResponse>> getAdsGrouped() {
        AdsGroupedResponse adsGrouped = adService.getAdsByStatusGrouped();
        return ResponseEntity.ok(ApiResponseGenerator.success(adsGrouped));
    }

    // 2. 신청한 광고(캠페인) 전체 조회
    @GetMapping("/applied")
    public ResponseEntity<ApiResponse<AppliedAdsResponse>> getAppliedAds() {
        Long userNo = securityUtil.getCurrentUserNo();
        AppliedAdsResponse ads = adService.getAppliedAds(userNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(ads));
    }

    // 3-1. 광고 이미지 조회
    @GetMapping("/image/{adNo}")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> getImageByAdNo(@PathVariable Long adNo) {
        ImageUploadResponse image = adService.getImageByAdNo(adNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(image));
    }

    // 3-2. 광고주 파일 조회
    @GetMapping("/file/{advertiserNo}")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> getAdvertiserFile(@PathVariable Long advertiserNo) {
        List<FileUploadResponse> image = adService.getAdvertiserFile(advertiserNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(image));
    }

    // 4. 광고 단일 조회
    @GetMapping("/{adNo}")
    public ResponseEntity<ApiResponse<AdResponse>> getAd(@PathVariable Long adNo) {
        AdResponse response = adService.getAd(adNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }
}

