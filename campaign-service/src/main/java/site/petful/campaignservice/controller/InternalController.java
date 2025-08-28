package site.petful.campaignservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.common.ApiResponseGenerator;
import site.petful.campaignservice.common.ErrorCode;
import site.petful.campaignservice.dto.campaign.ApplicantResponse;
import site.petful.campaignservice.dto.campaign.ApplicantsResponse;
import site.petful.campaignservice.entity.ApplicantStatus;
import site.petful.campaignservice.service.CampaignService;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private final CampaignService campaignService;

    public  InternalController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    // 2. 광고별 체험단 전체 조회 - 광고주
    @GetMapping("/{adNo}")
    public ResponseEntity<ApiResponse<?>> getApplicants(@PathVariable Long adNo) {
        try {
            ApplicantsResponse response = campaignService.getApplicants(adNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }

    // 3-2. 체험단 applicantStatus 수정 - 광고주
    @PutMapping("/advertiser/{applicantNo}")
    public ResponseEntity<ApiResponse<?>> updateApplicantByAdvertiser(
            @PathVariable Long applicantNo,
            @RequestParam ApplicantStatus status) {
        try {
            ApplicantResponse response = campaignService.updateApplicantByAdvertiser(applicantNo, status);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }
}
