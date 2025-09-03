package site.petful.campaignservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProfileResponse {
    private Long userNo;
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private String role;

    // 프로필 정보
    private String profileImageUrl;
    private String selfIntroduction;
    private LocalDate birthDate;
    private String roadAddress;
    private String detailAddress;
    private String instagramAccount;

    // 타임스탬프
    private LocalDateTime profileUpdatedAt;
}

