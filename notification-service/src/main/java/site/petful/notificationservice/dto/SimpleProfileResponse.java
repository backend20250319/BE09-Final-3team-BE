package site.petful.notificationservice.dto;

import lombok.Data;

@Data
public class SimpleProfileResponse {
    private Long id;          // userNo
    private String nickname;      // 닉네임
    private String profileImageUrl;// 프로필 이미지 URL
}
