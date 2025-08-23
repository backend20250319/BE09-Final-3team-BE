package site.petful.campaignservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.common.ApiResponseGenerator;
import site.petful.campaignservice.dto.advertisement.AdsGroupedResponse;
import site.petful.campaignservice.service.AdService;

@RestController
@RequestMapping("/ad")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    // 1. adStatus별(모집중/종료된) 광고(캠페인) 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAdsGrouped() {
        AdsGroupedResponse adsGrouped = adService.getAdsByStatusGrouped();
        return ResponseEntity.ok(ApiResponseGenerator.success(adsGrouped));
    }

}
