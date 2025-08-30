package site.petful.campaignservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.campaignservice.common.ApiResponse;
import site.petful.campaignservice.common.ApiResponseGenerator;
import site.petful.campaignservice.common.ErrorCode;
import site.petful.campaignservice.dto.ReviewRequest;
import site.petful.campaignservice.dto.ReviewResponse;
import site.petful.campaignservice.service.ReviewService;

@RestController
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // 2. 리뷰 조회
    @GetMapping("/{applicantNo}")
    public ResponseEntity<ApiResponse<?>> getReview(@PathVariable Long applicantNo) {
        try {
            ReviewResponse response = reviewService.getReview(applicantNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.APPLICANT_NOT_FOUND));
        }
    }

    // 3-1. 리뷰 수정 - 체험단 (review_url)
    @PatchMapping("/{applicantNo}")
    public ResponseEntity<ApiResponse<?>> updateReview(
            @PathVariable Long applicantNo,
            @RequestBody ReviewRequest request) {
        try {
            ReviewResponse response = reviewService.updateReview(applicantNo, request);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.APPLICANT_NOT_FOUND));
        }
    }

}
