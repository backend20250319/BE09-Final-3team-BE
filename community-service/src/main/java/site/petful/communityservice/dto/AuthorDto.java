package site.petful.communityservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AuthorDto {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("nickname")
    private String nickname;
    
    @JsonProperty("profileImageUrl")
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
                .nickname(u.getNickname() != null ? u.getNickname() : "익명")
                .profileImageUrl(u.getProfileImageUrl())
                .build();
    }
}
