package site.petful.snsservice.instagram.profile.dto;

import jakarta.validation.constraints.NotNull;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;

public record InstagramProfileDto(@NotNull Long id,
                                  @NotNull String username,
                                  @NotNull String name,
                                  @NotNull String profile_picture_url,
                                  @NotNull Long followers_count,
                                  @NotNull Long follows_count,
                                  @NotNull Long media_count,
                                  @NotNull Boolean auto_delete) {

    public static InstagramProfileDto fromEntity(InstagramProfileEntity entity) {
        return new InstagramProfileDto(
            entity.getId(),
            entity.getUsername(),
            entity.getName(),
            entity.getProfilePictureUrl(),
            entity.getFollowersCount(),
            entity.getFollowsCount(),
            entity.getMediaCount()
            , entity.getAutoDelete()
        );
    }
}
