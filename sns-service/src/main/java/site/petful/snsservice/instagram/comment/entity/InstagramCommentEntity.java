package site.petful.snsservice.instagram.comment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.snsservice.instagram.client.dto.InstagramApiCommentDto;
import site.petful.snsservice.instagram.media.entity.InstagramMediaEntity;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;

@Entity
@Table(name = "instagram_comments")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class InstagramCommentEntity {

    @Id
    private Long id;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private Long likeCount;
    @Column(nullable = false)
    private String text;
    @Column(nullable = false)
    private OffsetDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sentiment sentiment;

    @Column(nullable = false)
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instagram_media_id", nullable = false)
    private InstagramMediaEntity instagramMedia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instagram_media_id", nullable = false)
    private InstagramProfileEntity instagramProfile;

    public InstagramCommentEntity(InstagramApiCommentDto dto, Sentiment sentiment,
        Boolean isDeleted, InstagramMediaEntity instagramMedia,
        InstagramProfileEntity instagramProfileEntity) {
        this.id = dto.id();
        this.username = dto.username();
        this.likeCount = dto.likeCount();
        this.text = dto.text();
        this.timestamp = dto.timestamp();
        this.instagramMedia = instagramMedia;
        this.instagramProfile = instagramProfileEntity;
        this.sentiment = sentiment;
        this.isDeleted = isDeleted;
    }

    public void delete() {
        if (this.isDeleted != null && this.isDeleted) {
            throw new IllegalStateException("이미 삭제된 댓글입니다.");
        }
        this.isDeleted = true;
    }

    public void update(InstagramApiCommentDto dto) {
        this.username = dto.username();
        this.likeCount = dto.likeCount();
        this.text = dto.text();
        this.timestamp = dto.timestamp();
        isDeleted = false;
    }
}
