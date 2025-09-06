package site.petful.advertiserservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.advertiserservice.client.CampaignFeignClient;
import site.petful.advertiserservice.common.ApiResponse;
import site.petful.advertiserservice.common.ErrorCode;
import site.petful.advertiserservice.dto.campaign.ApplicantsResponse;
import site.petful.advertiserservice.dto.campaign.ReviewRequest;
import site.petful.advertiserservice.dto.campaign.ReviewResponse;
import site.petful.advertiserservice.entity.ApplicantStatus;
import site.petful.advertiserservice.repository.AdRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final AdRepository adRepository;
    private final CampaignFeignClient campaignFeignClient;

    // 1. 광고별 체험단 리뷰 조회
    public List<ReviewResponse> getReviewByAdNo(Long adNo) {

        adRepository.findByAdNo(adNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.AD_NOT_FOUND.getDefaultMessage()));

        ApplicantsResponse response = campaignFeignClient.getApplicants(adNo).getData();

        List<ReviewResponse> reviews = response.getApplicants().stream()
                .filter(applicant -> applicant.getStatus() == ApplicantStatus.SELECTED)
                .map(applicant -> {
                    Long applicantNo = applicant.getApplicantNo();
                    ApiResponse<ReviewResponse> res = campaignFeignClient.getReview(applicantNo);
                    return res.getData();
                })
                .collect(Collectors.toList());

        return reviews;
    }

    // 1-2. 체험단 개별 리뷰 조회
    public ReviewResponse getReview(Long applicantNo) {
        return campaignFeignClient.getReview(applicantNo).getData();
    }

    // 2. 체험단 리뷰 승인/반려
    @Transactional
    public ReviewResponse updateReview(Long applicantNo, ReviewRequest request) {
        return campaignFeignClient.updateReview(applicantNo, request).getData();
     }

}
