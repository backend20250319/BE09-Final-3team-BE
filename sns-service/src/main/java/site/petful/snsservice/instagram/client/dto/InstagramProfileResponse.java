package site.petful.snsservice.instagram.client.dto;

public record InstagramProfileResponse(Long id,
                                       String username,
                                       String name,
                                       String profile_picture_url,
                                       Integer followers_count,
                                       Integer follows_count,
                                       Integer media_count) {

}
