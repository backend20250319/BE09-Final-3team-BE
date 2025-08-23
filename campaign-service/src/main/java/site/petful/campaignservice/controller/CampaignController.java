package site.petful.campaignservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.common.ApiResponseGenerator;
import site.petful.campaignservice.dto.advertisement.AdsGroupedResponse;
import site.petful.campaignservice.service.CampaignService;

@RestController
@RequestMapping("/campaign")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    // 1. adStatus별(모집중/종료된) 광고(캠페인) 전체 조회
    @GetMapping("/ad")
    public ResponseEntity<ApiResponse<?>> getAdsGrouped() {
        AdsGroupedResponse adsGrouped = campaignService.getAdsByStatusGrouped();
        return ResponseEntity.ok(ApiResponseGenerator.success(adsGrouped));
    }

}
