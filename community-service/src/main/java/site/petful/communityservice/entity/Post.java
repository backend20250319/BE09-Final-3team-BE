package site.petful.communityservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name="Post")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="post_no")
    private Long Id;

    @Column(name="user_no" , nullable = false)
    private Long userId;

    @Column(name="title" , nullable = false , length = 100)
    private String title;

    @Column(name="content" , nullable = false)
    private String content;

    @Column(name="created_at",updatable = false , nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name="type" , nullable = false , length = 30)
    private PostType type;

    @Enumerated(EnumType.STRING)
    @Column(name="post_status", nullable = false)
    private PostStatus status = PostStatus.PUBLISHED;
    public Post(Long userId, String title, String content, PostType type) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

}
