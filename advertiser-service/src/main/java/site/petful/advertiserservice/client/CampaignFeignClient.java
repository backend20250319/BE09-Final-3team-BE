package site.petful.advertiserservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.config.ServiceFeignConfig;
import site.petful.advertiserservice.dto.campaign.*;

@FeignClient(name = "campaign-service", path = "/internal", configuration = ServiceFeignConfig.class)
public interface CampaignFeignClient {

    // 1. 광고별 체험단 전체 조회 - 광고주
    @GetMapping("/{adNo}")
    ApiResponse<ApplicantsResponse> getApplicants(@PathVariable("adNo") Long adNo);

    // 2. 체험단 applicantStatus 수정
    @PutMapping("/applicant/{applicantNo}")
    ApiResponse<ApplicantResponse> updateApplicant(@PathVariable Long applicantNo, @RequestBody ApplicantRequest request);

    /* 리뷰 API */
    // 3. 리뷰 생성
    @PostMapping("/review/{applicantNo}")
    ApiResponse<ReviewResponse> createReview(@PathVariable Long applicantNo);

    // 4. 리뷰 조회
    @GetMapping("/review/{applicantNo}")
    ApiResponse<ReviewResponse> getReview(@PathVariable Long applicantNo);

    // 5. 리뷰 수정
    @PutMapping("/review/{applicantNo}")
    ApiResponse<ReviewResponse> updateReview(@PathVariable Long applicantNo, @RequestBody ReviewRequest request);
}
