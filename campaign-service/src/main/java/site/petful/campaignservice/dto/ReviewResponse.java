package site.petful.campaignservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import site.petful.campaignservice.entity.Review;
import site.petful.campaignservice.entity.ReviewStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewResponse {

    private Long applicantNo;
    private String reviewUrl;
    private ReviewStatus isApproved;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewResponse from(Review review) {
        ReviewResponse res = new ReviewResponse();
        res.setApplicantNo(review.getApplicantNo());
        res.setReviewUrl(review.getReviewUrl());
        res.setIsApproved(review.getIsApproved());
        res.setReason(review.getReason());
        res.setCreatedAt(review.getCreatedAt());
        res.setUpdatedAt(review.getUpdatedAt());

        return res;
    }
}
