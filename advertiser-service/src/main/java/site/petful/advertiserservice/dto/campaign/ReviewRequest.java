package site.petful.advertiserservice.dto.campaign;

import lombok.Getter;

@Getter
public class ReviewRequest {

    private String reviewUrl;
    private String reason;
    private Boolean isApproved;
}
