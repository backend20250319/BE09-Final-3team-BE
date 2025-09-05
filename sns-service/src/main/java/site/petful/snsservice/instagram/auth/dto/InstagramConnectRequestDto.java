package site.petful.snsservice.instagram.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InstagramConnectRequestDto(

    @NotNull @NotBlank String accessToken) {

}
