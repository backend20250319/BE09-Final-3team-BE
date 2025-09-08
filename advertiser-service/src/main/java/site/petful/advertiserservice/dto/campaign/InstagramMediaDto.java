package site.petful.advertiserservice.dto.campaign;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class InstagramMediaDto {

    private Long id;
    private String caption;
    private String mediaType;
    private String mediaUrl;
    private String permalink;
    private OffsetDateTime timestamp;
    private boolean isCommentEnabled;
    private long likeCount;
    private long commentsCount;
}
