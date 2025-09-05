// CommentPageDto.java
package site.petful.communityservice.dto;

import java.util.List;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CommentPageDto {
    private List<CommentView> content;
    private int page;
    private int size;
    private long totalElements;
    private boolean last;
}
