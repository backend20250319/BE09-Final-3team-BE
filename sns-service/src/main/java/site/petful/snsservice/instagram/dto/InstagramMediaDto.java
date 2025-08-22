package site.petful.snsservice.instagram.dto;

import java.time.OffsetDateTime;

public record InstagramMediaDto(Long id, String caption, String mediaType,
                                String mediaUrl, String permalink, OffsetDateTime timestamp,
                                boolean isCommentEnabled, long likeCount, long commentsCount) {


}