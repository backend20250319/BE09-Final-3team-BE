package site.petful.communityservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.communityservice.entity.Post;
import site.petful.communityservice.entity.PostType;

import java.time.LocalDateTime;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class PostItem {
    private Long postId;
    private String title;
    private String contentPreview;
    private PostType type;
    private LocalDateTime createdAt;
    private int commentCount;
    private AuthorDto author;
    public static PostItem from(Post p , long commentCount , UserBriefDto u){
        String preview = p.getContent();
        if (preview != null && preview.length() > 100) {
            preview = preview.substring(0, 100) + "... ";
        }
        return PostItem.builder()
                .postId(p.getId())
                .title(p.getTitle())
                .contentPreview(preview)
                .createdAt(p.getCreatedAt())
                .type(p.getType())
                .author(AuthorDto.from(u))
                .commentCount((int) commentCount)
                .build();
    }
}
