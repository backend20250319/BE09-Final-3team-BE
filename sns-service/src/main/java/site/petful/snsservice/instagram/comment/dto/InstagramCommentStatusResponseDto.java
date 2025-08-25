package site.petful.snsservice.instagram.comment.dto;

public record InstagramCommentStatusResponseDto(
    Long totalComments,
    Long autoDeletedComments,
    Double autoDeleteRate,
    Long bannedWordComments
) {
    
}
