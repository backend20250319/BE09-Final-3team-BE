package site.petful.communityservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.petful.communityservice.entity.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentNode {
    private Long commentId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private List<CommentNode> children = new ArrayList<>();

    public static CommentNode of(Comment c){
        return new CommentNode(
                c.getId(), c.getUserId(), c.getContent(), c.getCreatedAt(), new ArrayList<>()
        );
    }
}
