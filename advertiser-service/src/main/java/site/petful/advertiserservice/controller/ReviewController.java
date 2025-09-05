package site.petful.advertiserservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ApiResponseGenerator;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.campaign.ReviewRequest;
import site.petful.advertiserservice.dto.campaign.ReviewResponse;
import site.petful.advertiserservice.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // 1. 광고별 체험단 리뷰 조회
    @GetMapping("/{adNo}")
    public ResponseEntity<ApiResponse<?>> getReviewByAdNo(@PathVariable Long adNo) {
        try{
            List<ReviewResponse> response = reviewService.getReviewByAdNo(adNo);
            return ResponseEntity.ok(ApiResponseGenerator.success(response));
        } catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseGenerator.fail(ErrorCode.AD_NOT_FOUND));
        }
    }


    // 2. 체험단 리뷰 승인/반려
    @PutMapping("/{applicantNo}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long applicantNo,
            @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.updateReview(applicantNo, request);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }
}
