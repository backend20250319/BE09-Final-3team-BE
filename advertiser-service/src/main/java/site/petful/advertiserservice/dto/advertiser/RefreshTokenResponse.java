package site.petful.advertiserservice.dto.advertiser;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshTokenResponse {
    private String accessToken;
    private String message;
}


