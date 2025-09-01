// CommentView.java
package site.petful.communityservice.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import site.petful.communityservice.entity.CommentStatus;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CommentView {
    private Long id;
    private Long parentId;
    private Long userId;
    private AuthorDto author;      // 작성자 묶음
    private String content;
    private LocalDateTime createdAt;
    private CommentStatus commentStatus;
    @Builder.Default
    private List<CommentView> children = new ArrayList<>(); // 대댓글(1단계까지만)
}
