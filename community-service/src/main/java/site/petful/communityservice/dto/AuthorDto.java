package site.petful.communityservice.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AuthorDto {
    private Long id;
    private String nickname;
    private String profileImageUrl;
}
