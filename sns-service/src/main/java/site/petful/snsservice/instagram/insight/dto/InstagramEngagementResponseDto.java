package site.petful.snsservice.instagram.insight.dto;

public record InstagramEngagementResponseDto(
    Double likeRate,
    Double commentRate,
    Double shareRate
) {

}
