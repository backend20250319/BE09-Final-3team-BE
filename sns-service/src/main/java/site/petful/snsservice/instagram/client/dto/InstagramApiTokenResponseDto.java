package site.petful.snsservice.instagram.client.dto;

import jakarta.validation.constraints.NotNull;

public record InstagramApiTokenResponseDto(
    @NotNull String access_token,
    @NotNull String token_type,
    Long expires_in
) {

}
