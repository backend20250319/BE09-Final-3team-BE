package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.advertiserservice.client.CampaignFeignClient;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.dto.campaign.ApplicantResponse;
import site.petful.advertiserservice.dto.campaign.ApplicantsResponse;
import site.petful.advertiserservice.entity.ApplicantStatus;

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
    public ApplicantResponse updateApplicant(Long applicantNo, ApplicantStatus status) {
        ApiResponse<ApplicantResponse> response = campaignFeignClient.updateApplicantByAdvertiser(applicantNo, status);
        if (status == ApplicantStatus.SELECTED) {
            campaignFeignClient.createReview(applicantNo);
        }
        return response.getData();
    }
}
