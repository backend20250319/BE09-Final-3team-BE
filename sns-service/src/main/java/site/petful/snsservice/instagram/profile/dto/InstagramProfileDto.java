package site.petful.snsservice.instagram.profile.dto;

import jakarta.validation.constraints.NotNull;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;

public record InstagramProfileDto(@NotNull Long id,
                                  @NotNull String username,
                                  @NotNull String name,
                                  @NotNull String profile_picture_url,
                                  @NotNull Integer followers_count,
                                  @NotNull Integer follows_count,
                                  @NotNull Integer media_count) {

    public static InstagramProfileDto fromEntity(InstagramProfileEntity entity) {
        return new InstagramProfileDto(
            entity.getId(),
            entity.getUsername(),
            entity.getName(),
            entity.getProfilePictureUrl(),
            entity.getFollowersCount(),
            entity.getFollowsCount(),
            entity.getMediaCount()
        );
    }
}
