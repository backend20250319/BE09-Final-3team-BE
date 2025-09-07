package site.petful.advertiserservice.dto.campaign;

import lombok.Getter;
import site.petful.advertiserservice.entity.ReviewStatus;

@Getter
public class ReviewRequest {

    private String reviewUrl;
    private String reason;
    private ReviewStatus isApproved;
}
