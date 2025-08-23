package site.petful.campaignservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.common.ApiResponseGenerator;
import site.petful.campaignservice.common.ErrorCode;
import site.petful.campaignservice.dto.campaign.ApplicantResponse;
import site.petful.campaignservice.dto.campaign.CampaignRequest;
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
        try {
            ApplicantResponse response = campaignService.applyCampaign(adNo, petNo, request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }

    // 2. 광고별 체험단 전체 조회 - 광고주


    // 3-1. 체험단 추가 내용 수정 - 체험단
    @PutMapping("/applicant/{applicantNo}")
    public ResponseEntity<ApiResponse<?>> updateCampaign(
            @PathVariable Long applicantNo,
            @RequestBody CampaignRequest request) {
        try {
            ApplicantResponse response = campaignService.updateCampaign(applicantNo, request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }

    // 3-2. 체험단 applicantStatus 수정 - 광고주

    // 4. 체험단 신청 취소(삭제)
    @DeleteMapping("/{applicantNo}")
    public ResponseEntity<ApiResponse<?>> deleteCampaign(@PathVariable Long applicantNo){
        try {
            campaignService.deleteCampaign(applicantNo);
            return ResponseEntity.ok(ApiResponseGenerator.success());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }
}
