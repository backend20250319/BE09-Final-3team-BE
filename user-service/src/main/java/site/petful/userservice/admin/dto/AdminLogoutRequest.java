package site.petful.userservice.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLogoutRequest {
    
    @NotBlank(message = "리프레시 토큰은 필수 입력 항목입니다.")
    private String refreshToken;
}
