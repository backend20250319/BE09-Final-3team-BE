package site.petful.snsservice.instagram.dto;

import java.time.OffsetDateTime;

public record InstagramCommentDto(long id, String username, long likeCount,
                                  String text,
                                  OffsetDateTime timestamp) {

}
