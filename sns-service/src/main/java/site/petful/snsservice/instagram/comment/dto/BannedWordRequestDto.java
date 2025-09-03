package site.petful.snsservice.instagram.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BannedWordRequestDto(
    @NotNull Long instagramId,
    @NotNull @NotBlank String word) {

}
