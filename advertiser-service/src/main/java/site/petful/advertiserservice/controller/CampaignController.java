package site.petful.advertiserservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.campaign.ApplicantResponse;
import site.petful.advertiserservice.dto.campaign.ApplicantsResponse;
import site.petful.advertiserservice.entity.ApplicantStatus;
import site.petful.advertiserservice.service.CampaignService;

@RestController
@RequestMapping("/campaign")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    // 1. 체험단 조회
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

    // 2. 체험단 선정
    @PutMapping("/advertiser/{applicantNo}")
    public ResponseEntity<ApiResponse<?>> updateApplicant(
            @PathVariable Long applicantNo,
            @RequestParam ApplicantStatus status){
        try {
            ApplicantResponse response = campaignService.updateApplicant(applicantNo, status);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.APPLICANT_NOT_FOUND));
        }
    }
}
