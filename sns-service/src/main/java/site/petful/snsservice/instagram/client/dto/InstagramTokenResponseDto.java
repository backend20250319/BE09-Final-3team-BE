package site.petful.snsservice.instagram.client.dto;

public record InstagramTokenResponseDto(
    String access_token,
    String token_type,
    Long expires_in
) {

}
