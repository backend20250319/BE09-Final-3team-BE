package site.petful.snsservice.instagram.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import site.petful.snsservice.instagram.client.dto.InstagramProfileResponse;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class InstagramProfile {

    @Id
    private Long id;
    private String userName;
    private String name;
    @Column(name = "profile_picture_url", length = 512)
    private String profile_picture_url;
    private Integer followers_count;
    private Integer follows_count;
    private Integer media_count;


    public InstagramProfile(InstagramProfileResponse response) {
        this.id = response.id();
        this.userName = response.username();
        this.name = response.name();
        this.profile_picture_url = response.profile_picture_url();
        this.followers_count = response.followers_count();
        this.follows_count = response.follows_count();
        this.media_count = response.media_count();
    }

    public InstagramProfileResponse toResponse() {
        return new InstagramProfileResponse(
            id,
            userName,
            name,
            profile_picture_url,
            followers_count,
            follows_count,
            media_count
        );
    }
}