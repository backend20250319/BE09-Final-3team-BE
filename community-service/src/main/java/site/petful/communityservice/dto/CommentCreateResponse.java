package site.petful.communityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentCreateResponse {
    private Long commentId;
    private Long postId;
    private Long parentId;
    private Long userId;
    private LocalDateTime createdAt;
}
