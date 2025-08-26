package site.petful.snsservice.instagram.insight.dto;

public record InstagramFollowerHistoryResponseDto(
    Long instagramId,
    String statMonth,
    Long totalFollowers
) {

}
