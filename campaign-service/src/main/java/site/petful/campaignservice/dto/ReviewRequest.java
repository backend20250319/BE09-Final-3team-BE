package site.petful.campaignservice.dto;

import lombok.Getter;
import site.petful.campaignservice.entity.ReviewStatus;

@Getter
public class ReviewRequest {

    private String reviewUrl;
    private String reason;
    private ReviewStatus isApproved;
}
