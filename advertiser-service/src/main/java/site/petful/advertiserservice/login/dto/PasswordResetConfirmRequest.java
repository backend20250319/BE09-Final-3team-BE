package site.petful.advertiserservice.login.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetConfirmRequest {
    
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    private String email;
    
    @NotBlank(message = "인증 코드는 필수 입력 항목입니다.")
    private String verificationCode;
    
    @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String newPassword;
    
    @NotBlank(message = "새 비밀번호 확인은 필수 입력 항목입니다.")
    private String confirmPassword;
}
