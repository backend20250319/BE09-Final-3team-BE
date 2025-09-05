package site.petful.snsservice.instagram.media.dto;

import jakarta.validation.constraints.NotNull;

public record InstagramAnalysisMediasResponseDto(
    @NotNull Double averageLikes,
    @NotNull Double averageComments,
    @NotNull Long TopLikeCount,
    @NotNull Long TopCommentCount,
    @NotNull Double likesMoMPercent,
    @NotNull Double commentsMoMPercent,
    @NotNull Double topLikeMoMPercent,
    @NotNull Double topCommentMoMPercent
) {

}
