package site.petful.snsservice.instagram.media.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.snsservice.instagram.media.dto.InstagramMediaDto;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;


@Entity
@Table(name = "instagram_media")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InstagramMediaEntity {

    @Id
    private Long id;
    private String caption;
    @Column(nullable = false)
    private String mediaType;
    @Column(name = "profile_picture_url", length = 512, nullable = false)
    private String mediaUrl;
    @Column(nullable = false)
    private String permalink;
    @Column(nullable = false)
    private OffsetDateTime timestamp;
    @Column(nullable = false)
    private Boolean isCommentEnabled;
    @Column(nullable = false)
    private Long likeCount;
    @Column(nullable = false)
    private Long commentsCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instagram_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private InstagramProfileEntity instagramProfile;


    public InstagramMediaEntity(InstagramMediaDto instagramMediaDto,
        InstagramProfileEntity instagramProfile) {
        this.id = instagramMediaDto.id();
        this.caption = instagramMediaDto.caption();
        this.mediaType = instagramMediaDto.mediaType();
        this.mediaUrl = instagramMediaDto.mediaUrl();
        this.permalink = instagramMediaDto.permalink();
        this.timestamp = instagramMediaDto.timestamp();
        this.isCommentEnabled = instagramMediaDto.isCommentEnabled();
        this.likeCount = instagramMediaDto.likeCount();
        this.commentsCount = instagramMediaDto.commentsCount();
        this.instagramProfile = instagramProfile;
    }


    public InstagramMediaDto toDto() {
        return new InstagramMediaDto(id, caption, mediaType, mediaUrl, permalink, timestamp,
            isCommentEnabled, likeCount, commentsCount);
    }

    public void update(InstagramMediaDto dto) {
        this.caption = dto.caption();
        this.mediaType = dto.mediaType();
        this.mediaUrl = dto.mediaUrl();
        this.permalink = dto.permalink();
        this.timestamp = dto.timestamp();
        this.isCommentEnabled = dto.isCommentEnabled();
        this.likeCount = dto.likeCount();
        this.commentsCount = dto.commentsCount();
    }
}