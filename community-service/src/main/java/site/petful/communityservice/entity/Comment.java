package site.petful.communityservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Comment")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="comment_no")
    private Long id;

    @Column(name = "user_no", nullable = false)
    private Long userId;

    @Column(name ="post_no" , nullable = false)
    private Long postId;

    @Column(name ="content",nullable = false)
    private String content;

    @Column(name="created_at",nullable = false)
    private LocalDateTime createdAt;

    @Column(name ="parent_no")
    private Long parentId;

    @Enumerated(EnumType.STRING)
    private CommentStatus commentStatus = CommentStatus.NORMAL;
    @PrePersist
    protected void onCreate() {if(createdAt == null) createdAt = LocalDateTime.now();}
}
