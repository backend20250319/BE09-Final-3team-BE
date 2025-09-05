package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.advertiserservice.client.CampaignFeignClient;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.dto.campaign.ApplicantRequest;
import site.petful.advertiserservice.dto.campaign.ApplicantResponse;
import site.petful.advertiserservice.dto.campaign.ApplicantsResponse;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignFeignClient campaignFeignClient;

    // 1. 체험단 조회
    public ApplicantsResponse getApplicants(Long adNo) {
        ApiResponse<ApplicantsResponse> response = campaignFeignClient.getApplicants(adNo);
        return response.getData();
    }

    // 2. 체험단 선정
    @Transactional
    public ApplicantResponse updateApplicant(Long applicantNo, ApplicantRequest request) {
        ApiResponse<ApplicantResponse> response = campaignFeignClient.updateApplicant(applicantNo, request);
        return response.getData();
    }
}
