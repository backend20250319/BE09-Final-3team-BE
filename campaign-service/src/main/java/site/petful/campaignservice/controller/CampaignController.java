package site.petful.campaignservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.common.ApiResponseGenerator;
import site.petful.campaignservice.common.ErrorCode;
import site.petful.campaignservice.dto.campaign.ApplicantResponse;
import site.petful.campaignservice.dto.campaign.ApplicantRequest;
import site.petful.campaignservice.dto.campaign.ApplicantsResponse;
import site.petful.campaignservice.security.SecurityUtil;
import site.petful.campaignservice.service.CampaignService;

import java.util.List;

@RestController
@RequestMapping("/campaign")
public class CampaignController {

    private final CampaignService campaignService;
    private final SecurityUtil securityUtil;

    public CampaignController(CampaignService campaignService, SecurityUtil securityUtil) {
        this.campaignService = campaignService;
        this.securityUtil = securityUtil;
    }

    // 1. 체험단 신청
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<?>> applyCampaign(
            @RequestParam Long adNo,
            @RequestParam Long petNo,
            @RequestBody ApplicantRequest request) {
        try {
            ApplicantResponse response = campaignService.applyCampaign(adNo, petNo, request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND, e.getMessage()));
        }
    }

    // 2-2. petNo로 체험단 신청 내역 조회
    @GetMapping("/{petNo}")
    public ResponseEntity<ApiResponse<?>> getApplicantsByPetNo(@PathVariable Long petNo) {
        try {
            ApplicantsResponse response = campaignService.getApplicantsByPetNo(petNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }

    // 2-3. 광고 + 사용자별 신청자 조회
    @GetMapping("/users/{adNo}")
    public ResponseEntity<ApiResponse<?>> getApplicantsByAd(@PathVariable Long adNo) {
        try {
            Long userNo = securityUtil.getCurrentUserNo();
            List<ApplicantResponse> response = campaignService.getApplicantsByAd(userNo, adNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }

    // 3. 체험단 추가 내용/ adStatus 수정
    @PutMapping("/applicant/{applicantNo}")
    public ResponseEntity<ApiResponse<?>> updateApplicant(
            @PathVariable Long applicantNo,
            @RequestBody ApplicantRequest request) {
        try {   ApplicantResponse response = campaignService.updateApplicant(applicantNo, request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }

    // 4. 체험단 신청 취소
    @DeleteMapping("/{applicantNo}")
    public ResponseEntity<ApiResponse<?>> cancelApplicant(@PathVariable Long applicantNo){
        try {
            campaignService.cancelApplicant(applicantNo);
            return ResponseEntity.ok(ApiResponseGenerator.success());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }

    // 4-2. 체험단 소프트 삭제
    @PutMapping("/delete/{applicantNo}")
    public ResponseEntity<ApiResponse<?>> deleteApplicant(
            @PathVariable Long applicantNo,
            @RequestParam Boolean isDeleted) {
        try {
            ApplicantResponse response = campaignService.deleteApplicant(applicantNo, isDeleted);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }

}
