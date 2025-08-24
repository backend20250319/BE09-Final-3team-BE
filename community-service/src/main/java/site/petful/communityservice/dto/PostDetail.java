package site.petful.communityservice.dto;

import lombok.*;
import site.petful.communityservice.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostDetail {
    private  Long postId;
    private  Long userId;
    private String title;
    private String content;
    private String type;
    private LocalDateTime createdAt;

    private boolean mine;
    private int commentCount;
    private List<CommentNode> comments;

    public static PostDetail from(Post p, Long userNo, int commentCount) {
        return PostDetail.builder()
                .postId(p.getId())
                .userId(p.getUserId())
                .title(p.getTitle())
                .content(p.getContent())
                .type(p.getType().name())
                .createdAt(p.getCreatedAt())
                .mine(userNo != null && p.getUserId().equals(userNo))
                .commentCount(commentCount)
                .build();
    }
    public PostDetail withComments(List<CommentNode> comments) {
        this.comments = comments;
        return this;
    }
}
