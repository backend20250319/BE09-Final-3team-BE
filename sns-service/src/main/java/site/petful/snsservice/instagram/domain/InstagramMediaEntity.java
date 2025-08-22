package site.petful.snsservice.instagram.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import site.petful.snsservice.instagram.dto.InstagramMediaDto;


@Entity
@Table(name = "instagram_media")
@AllArgsConstructor
@NoArgsConstructor
public class InstagramMediaEntity {

    @Id
    private Long id;
    private String caption;
    private String mediaType;
    @Column(name = "profile_picture_url", length = 512)
    private String mediaUrl;
    private String permalink;
    private OffsetDateTime timestamp;
    private Boolean isCommentEnabled;
    private Long likeCount;
    private Long commentsCount;
    @ManyToOne
    @JoinColumn(name = "instagram_id")
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


    public InstagramMediaDto toInstagramMediaDto() {
        return new InstagramMediaDto(id, caption, mediaType, mediaUrl, permalink, timestamp,
            isCommentEnabled, likeCount, commentsCount);
    }
}