package site.petful.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    
    @Size(max = 500, message = "프로필 이미지 URL은 500자를 초과할 수 없습니다.")
    private String profileImageUrl;
    
    @Size(max = 1000, message = "자기소개는 1000자를 초과할 수 없습니다.")
    private String selfIntroduction;
    
    private LocalDate birthDate;
    
    @Size(max = 255, message = "주소는 255자를 초과할 수 없습니다.")
    private String roadAddress;
    
    @Size(max = 255, message = "상세주소는 255자를 초과할 수 없습니다.")
    private String detailAddress;
    
    @Size(max = 100, message = "인스타그램 계정은 100자를 초과할 수 없습니다.")
    private String instagramAccount;

}



