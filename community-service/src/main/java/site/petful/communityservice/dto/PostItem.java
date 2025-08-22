package site.petful.communityservice.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.communityservice.entity.Post;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostItem {
    private Long id;
    private String title;
    private String contentPeview;
    private String type;
    private LocalDateTime createdAt;
    private int commentCount;
    public static PostItem from(Post p , int commentCount){
        String preview = p.getContent();
        if(preview != null && preview.length() > 100 ) {
            preview = preview.substring(0, 100) + "... ";
        }
        return new PostItem(
                p.getId(), p.getTitle(), preview,
                p.getType().name(),p.getCreatedAt(),commentCount
        );
    }
}
