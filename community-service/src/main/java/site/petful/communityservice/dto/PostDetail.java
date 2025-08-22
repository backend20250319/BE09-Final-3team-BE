package site.petful.communityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import site.petful.communityservice.entity.Post;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostDetail {
    private  Long postId;
    private  Long userId;
    private String title;
    private String content;
    private String type;
    private LocalDateTime createdAt;

    private boolean mine;
    private int commentCount;

    public static PostDetail from(Post p, Long userNo, int commentCount) {
        return new PostDetail(
                p.getId(), p.getUserId(), p.getTitle(), p.getContent(),
                p.getType().name(), p.getCreatedAt(),
                userNo != null && p.getUserId().equals(userNo),
                commentCount
        );
    }
}
