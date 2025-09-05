package site.petful.communityservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("postId")
    private Long postId;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("contentPreview")
    private String contentPreview;
    
    @JsonProperty("type")
    private PostType type;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("commentCount")
    private int commentCount;
    
    @JsonProperty("author")
    private AuthorDto author;
    
    public static PostItem from(Post p , long commentCount , UserBriefDto u){
        String preview = p.getContent();
        if (preview != null && preview.length() > 100) {
            preview = preview.substring(0, 100) + "... ";
        }
        
        // author가 null이 되지 않도록 보장
        AuthorDto authorDto = AuthorDto.from(u);
        
        return PostItem.builder()
                .postId(p.getId())
                .title(p.getTitle())
                .contentPreview(preview)
                .createdAt(p.getCreatedAt())
                .type(p.getType())
                .author(authorDto)
                .commentCount((int) commentCount)
                .build();
    }
}
