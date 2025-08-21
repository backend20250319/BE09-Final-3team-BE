package site.petful.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long accessExpiresAt;   // epoch millis
    private Long refreshExpiresAt;  // epoch millis

    private String message;
    private String email;
    private String name;
}
