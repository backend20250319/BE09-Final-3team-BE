package site.petful.communityservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateRequest {
    private Long postId;
    private Long parentId;
    private String content;
}
