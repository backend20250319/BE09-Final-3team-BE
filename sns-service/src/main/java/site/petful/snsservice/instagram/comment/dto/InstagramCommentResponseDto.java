package site.petful.snsservice.instagram.comment.dto;

import jakarta.validation.constraints.NotNull;
import site.petful.snsservice.instagram.comment.entity.InstagramCommentEntity;

public record InstagramCommentResponseDto(
    @NotNull Long id,
    @NotNull String username,
    @NotNull Long likeCount,
    @NotNull String text,
    @NotNull String timestamp,
    @NotNull String sentiment,
    @NotNull Boolean isDeleted
) {

    public static InstagramCommentResponseDto fromEntity(
        InstagramCommentEntity entity) {
        return new InstagramCommentResponseDto(
            entity.getId(),
            entity.getUsername(),
            entity.getLikeCount(),
            entity.getText(),
            entity.getTimestamp().toString(),
            entity.getSentiment().toString(),
            entity.getIsDeleted()
        );
    }
}
