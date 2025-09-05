package site.petful.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfoResponse {
    
    private String accessToken;
    private String refreshToken;
    private LocalDateTime accessExpiresAt;
    private LocalDateTime refreshExpiresAt;
    private long accessExpiresIn; // 초 단위
    private long refreshExpiresIn; // 초 단위
    private String tokenType;
    private String message;
    private String userType;  // 사용자 타입 추가
}
