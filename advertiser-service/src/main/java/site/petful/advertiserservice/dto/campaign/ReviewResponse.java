package site.petful.advertiserservice.dto.campaign;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewResponse {

    private Long applicantNo;
    private String reviewUrl;
    private Boolean isApproved;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
