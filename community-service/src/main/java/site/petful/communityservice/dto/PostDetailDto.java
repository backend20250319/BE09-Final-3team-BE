package site.petful.communityservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import site.petful.communityservice.entity.Post;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PostDetailDto {
    @JsonProperty("postId")
    private Long postId;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("author")
    private AuthorDto author;
    
    @JsonProperty("mine")
    private boolean mine;
    
    @JsonProperty("commentCount")
    private int commentCount;

    public static PostDetailDto from(Post p, int commentCount, UserBriefDto author, boolean isMine) {
        return PostDetailDto.builder()
                .postId(p.getId())
                .userId(p.getUserId())
                .title(p.getTitle())
                .content(p.getContent())
                .type(p.getType().name())
                .createdAt(p.getCreatedAt())
                .mine(isMine)
                .author(AuthorDto.from(author))
                .commentCount(commentCount)
                .build();
    }
}
