package site.petful.snsservice.instagram.client.dto;

import java.time.OffsetDateTime;

public record InstagramApiCommentDto(long id, String username, long likeCount,
                                     String text,
                                     OffsetDateTime timestamp) {

}
