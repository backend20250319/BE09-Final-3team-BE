package site.petful.snsservice.instagram.comment.dto;

import java.time.OffsetDateTime;

public record InstagramCommentDto(long id, String username, long likeCount,
                                  String text,
                                  OffsetDateTime timestamp) {

}
