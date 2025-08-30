package site.petful.snsservice.instagram.profile.dto;

import jakarta.validation.constraints.NotNull;

public record AutoDeleteRequestDto(
    @NotNull Long instagramId,
    @NotNull Boolean isAutoDelete
) {


}
