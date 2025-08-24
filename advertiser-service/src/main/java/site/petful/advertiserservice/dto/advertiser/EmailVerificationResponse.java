package site.petful.advertiserservice.dto.advertiser;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailVerificationResponse {
    private String message;
    private boolean success;
}

