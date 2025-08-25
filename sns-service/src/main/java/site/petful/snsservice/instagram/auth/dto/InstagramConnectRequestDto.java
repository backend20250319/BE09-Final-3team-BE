package site.petful.snsservice.instagram.auth.dto;

import jakarta.validation.constraints.NotNull;

public record InstagramConnectRequestDto(

    @NotNull String accessToken) {

}
