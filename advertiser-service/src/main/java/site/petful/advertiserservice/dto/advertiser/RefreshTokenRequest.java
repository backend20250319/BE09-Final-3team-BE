package site.petful.advertiserservice.dto.advertiser;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {
    
    @NotBlank(message = "Refresh Token은 필수입니다.")
    private String refreshToken;
}

