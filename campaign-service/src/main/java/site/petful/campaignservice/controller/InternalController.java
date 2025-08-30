package site.petful.campaignservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.common.ApiResponseGenerator;
import site.petful.campaignservice.common.ErrorCode;
import site.petful.campaignservice.dto.ReviewRequest;
import site.petful.campaignservice.dto.ReviewResponse;
import site.petful.campaignservice.dto.campaign.ApplicantResponse;
import site.petful.campaignservice.dto.campaign.ApplicantsResponse;
import site.petful.campaignservice.entity.ApplicantStatus;
import site.petful.campaignservice.service.CampaignService;
import site.petful.campaignservice.service.ReviewService;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private final CampaignService campaignService;
    private final ReviewService reviewService;

    public  InternalController(CampaignService campaignService, ReviewService reviewService) {
        this.campaignService = campaignService;
        this.reviewService = reviewService;
    }

    /* 체험단 API */
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

    /* 리뷰 API */
    // 1. 리뷰 생성
    @PostMapping("/review/{applicantNo}")
    public ResponseEntity<ApiResponse<?>> createReview(@PathVariable Long applicantNo) {
        try {
            ReviewResponse response = reviewService.createReview(applicantNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.APPLICANT_NOT_FOUND));
        }
    }

    // 2. 리뷰 조회
    @GetMapping("/review/{applicantNo}")
    public ResponseEntity<ApiResponse<?>> getReview(@PathVariable Long applicantNo) {
        try {
            ReviewResponse response = reviewService.getReview(applicantNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.APPLICANT_NOT_FOUND));
        }
    }

    // 3. 리뷰 수정 - 광고주
    @PutMapping("/review/{applicantNo}")
    public ResponseEntity<ApiResponse<?>> updateReview(
            @PathVariable Long applicantNo,
            @RequestBody ReviewRequest request) {
        try {
            ReviewResponse response = reviewService.updateReview(applicantNo, request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.APPLICANT_NOT_FOUND, e.getMessage()));
        }
    }
}
