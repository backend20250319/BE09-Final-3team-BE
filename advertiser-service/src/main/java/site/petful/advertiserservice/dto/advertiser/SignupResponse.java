package site.petful.advertiserservice.dto.advertiser;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {
    private Long userNo;
    private String userType;
    private String accessToken;
    private String refreshToken;
    private String message;
}
