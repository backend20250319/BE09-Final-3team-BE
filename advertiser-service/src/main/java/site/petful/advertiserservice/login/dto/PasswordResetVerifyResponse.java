package site.petful.advertiserservice.login.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetVerifyResponse {
    
    private String email;
    private boolean isValid;
    private String message;
    private long remainingTime; // 남은 시간 (초)
}

