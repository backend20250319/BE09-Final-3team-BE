package site.petful.communityservice.dto;

import lombok.*;
import site.petful.communityservice.entity.Post;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PostDetailDto {
    private Long postId;
    private Long userId;
    private String title;
    private String content;
    private String type;
    private LocalDateTime createdAt;
    private boolean mine;
    private int commentCount;

    public static PostDetailDto from(Post p, Long me, int commentCount) {
        return PostDetailDto.builder()
                .postId(p.getId())
                .userId(p.getUserId())
                .title(p.getTitle())
                .content(p.getContent())
                .type(p.getType().name())
                .createdAt(p.getCreatedAt())
                .mine(p.getUserId() != null && p.getUserId().equals(me))
                .commentCount(commentCount)
                .build();
    }
}
