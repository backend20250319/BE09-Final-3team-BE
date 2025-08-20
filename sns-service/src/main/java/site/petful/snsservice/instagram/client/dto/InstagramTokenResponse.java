package site.petful.snsservice.instagram.client.dto;

public record InstagramTokenResponse(
    String access_token,
    String token_type,
    Long expires_in
) {

}
