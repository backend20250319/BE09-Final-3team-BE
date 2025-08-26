package site.petful.snsservice.instagram.insight.dto;

import site.petful.snsservice.instagram.insight.entity.InstagramInsightEntity;

public record InstagramInsightResponseDto(
    String month,
    Long shares,
    Long likes,
    Long comments,
    Long views,
    Long reach
) {


    public static InstagramInsightResponseDto fromEntity(InstagramInsightEntity entity) {
        return new InstagramInsightResponseDto(
            entity.getMonth().toString(),
            entity.getShares(),
            entity.getLikes(),
            entity.getComments(),
            entity.getViews(),
            entity.getReach()
        );
    }
}
