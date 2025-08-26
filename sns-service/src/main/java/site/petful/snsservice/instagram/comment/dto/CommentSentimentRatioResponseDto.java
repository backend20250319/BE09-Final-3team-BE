package site.petful.snsservice.instagram.comment.dto;

public record CommentSentimentRatioResponseDto(
    double positiveRatio,
    double negativeRatio,
    double neutralRatio
) {

}
