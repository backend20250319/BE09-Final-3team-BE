package site.petful.campaignservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.campaignservice.common.ErrorCode;
import site.petful.campaignservice.dto.ReviewRequest;
import site.petful.campaignservice.dto.ReviewResponse;
import site.petful.campaignservice.entity.Applicant;
import site.petful.campaignservice.entity.Review;
import site.petful.campaignservice.entity.ReviewStatus;
import site.petful.campaignservice.repository.CampaignRepository;
import site.petful.campaignservice.repository.ReviewRepository;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CampaignRepository campaignRepository;

    // 1. 리뷰 생성
    public ReviewResponse createReview(Long applicantNo) {

        Applicant applicant = campaignRepository.findApplicantByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.APPLICANT_NOT_FOUND.getDefaultMessage()));

        Review review = new Review();
        review.setApplicant(applicant);
        review.setReviewUrl(null);
        review.setIsApproved(ReviewStatus.PENDING);
        review.setReason(null);
        Review saved = reviewRepository.save(review);

        return ReviewResponse.from(saved);
    }

    // 2. 리뷰 조회
    public ReviewResponse getReview(Long applicantNo) {

        campaignRepository.findApplicantByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.APPLICANT_NOT_FOUND.getDefaultMessage()));

        Review review = reviewRepository.findReviewByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.REVIEW_NOT_FOUND.getDefaultMessage()));

        return ReviewResponse.from(review);
    }

    // 3. 리뷰 수정
    public ReviewResponse updateReview(Long applicantNo, ReviewRequest request) {

        campaignRepository.findApplicantByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.APPLICANT_NOT_FOUND.getDefaultMessage()));

        Review review = reviewRepository.findReviewByApplicantNo(applicantNo)
                .orElseThrow(() -> new RuntimeException(ErrorCode.REVIEW_NOT_FOUND.getDefaultMessage()));

        if (request.getReviewUrl() != null) {
            review.setReviewUrl(request.getReviewUrl());
        }
        if (request.getReason() != null) {
            review.setReason(request.getReason());
        }
        if (request.getIsApproved() != null) {
            review.setIsApproved(request.getIsApproved());
        }

        Review saved = reviewRepository.save(review);

        return ReviewResponse.from(saved);
    }
}
