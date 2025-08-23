package site.petful.snsservice.instagram.comment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.snsservice.instagram.comment.dto.InstagramCommentDto;
import site.petful.snsservice.instagram.media.entity.InstagramMediaEntity;

@Entity
@Table(name = "instagram_comments")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class InstagramCommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private Long likeCount;
    private String text;
    private OffsetDateTime timestamp;

    // ManyToOne 관계 설정
    @ManyToOne
    @JoinColumn(name = "instagram_media_id")
    private InstagramMediaEntity instagramMedia;

    public InstagramCommentEntity(InstagramCommentDto dto, InstagramMediaEntity instagramMedia) {
        this.id = dto.id();
        this.username = dto.username();
        this.likeCount = dto.likeCount();
        this.text = dto.text();
        this.timestamp = dto.timestamp();
        this.instagramMedia = instagramMedia;
    }

    public InstagramCommentDto toDto() {
        return new InstagramCommentDto(
            id,
            username,
            likeCount,
            text,
            timestamp
        );
    }
}
