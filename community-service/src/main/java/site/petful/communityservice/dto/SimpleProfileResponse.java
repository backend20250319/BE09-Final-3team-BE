package site.petful.communityservice.dto;

import lombok.Data;

@Data
public class SimpleProfileResponse {
    private Long id;          // userNo
    private String nickname;      // 닉네임
    private String profileImageUrl;// 프로필 이미지 URL
    private String email;     // 이메일 (emil -> email로 수정)
    private String phone;     // 전화번호
}
