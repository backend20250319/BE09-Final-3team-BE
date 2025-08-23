package site.petful.advertiserservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.dto.campaign.ApplicantResponse;
import site.petful.advertiserservice.dto.campaign.ApplicantsResponse;
import site.petful.advertiserservice.entity.ApplicantStatus;

@FeignClient(name = "campaign-service", url = "http://localhost:8000/api/v1/campaign-service/campaign")
public interface CampaignFeignClient {

    // 1. 광고별 체험단 전체 조회 - 광고주
    @GetMapping("/{adNo}")
    ApiResponse<ApplicantsResponse> getApplicants(@PathVariable("adNo") Long adNo);

    // 2. 체험단 applicantStatus 수정 - 광고주
    @PutMapping("/advertiser/{applicantNo}")
    ApiResponse<ApplicantResponse> updateApplicantByAdvertiser(@PathVariable Long applicantNo, @RequestParam ApplicantStatus status);
}
