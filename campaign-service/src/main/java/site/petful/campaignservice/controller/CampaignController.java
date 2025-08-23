package site.petful.campaignservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.common.ApiResponseGenerator;
import site.petful.campaignservice.dto.campaign.CampaignRequest;
import site.petful.campaignservice.dto.campaign.CampaignResponse;
import site.petful.campaignservice.service.CampaignService;

@RestController
@RequestMapping("/campaign")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    // 1. 체험단 신청
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<?>> applyCampaign(
            @RequestParam Long adNo,
            @RequestParam Long petNo,
            @RequestBody CampaignRequest request) {
        CampaignResponse response = campaignService.applyAd(adNo, petNo, request);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }
}
