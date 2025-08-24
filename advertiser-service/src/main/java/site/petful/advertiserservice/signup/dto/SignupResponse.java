package site.petful.advertiserservice.signup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupResponse {
    private Long userNo;
    private String userType;
    private String accessToken;
    private String refreshToken;
    private String message;
}
