package site.petful.advertiserservice.dto.campaign;

import lombok.Getter;
import lombok.Setter;
import site.petful.advertiserservice.entity.ReviewStatus;

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
}
