package site.petful.advertiserservice.login.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private Long advertiserNo;
    private String userType;
    private String accessToken;
    private String refreshToken;
    private String message;
}
