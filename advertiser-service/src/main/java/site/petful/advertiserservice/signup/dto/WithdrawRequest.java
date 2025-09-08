package site.petful.advertiserservice.signup.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {
    
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;
    
    @NotBlank(message = "탈퇴 사유는 필수 입력 항목입니다.")
    private String reason;
}