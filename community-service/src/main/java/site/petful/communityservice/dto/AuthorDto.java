package site.petful.communityservice.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AuthorDto {
    private Long id;
    private String nickname;
    private String profileImageUrl;


    public static AuthorDto from(UserBriefDto u) {
        if (u == null) {
            return AuthorDto.builder()
                    .id(null)
                    .nickname("익명")
                    .profileImageUrl(null)
                    .build();
        }
        return AuthorDto.builder()
                .id(u.getId())
                .nickname(u.getNickname())
                .profileImageUrl(u.getProfileImageUrl())
                .build();
    }
}
