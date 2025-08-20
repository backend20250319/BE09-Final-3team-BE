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
    private Long userNo;
    private String userName;
    private String name;
    @Column(name = "profile_picture_url", length = 512)
    private String profilePictureUrl;
    private Integer followersCount;
    private Integer followsCount;
    private Integer mediaCount;


    public InstagramProfile(InstagramProfileResponse response, Long user_no) {
        this.id = response.id();
        this.userName = response.username();
        this.name = response.name();
        this.profilePictureUrl = response.profile_picture_url();
        this.followersCount = response.followers_count();
        this.followsCount = response.follows_count();
        this.mediaCount = response.media_count();
        this.userNo = user_no;
    }

    public InstagramProfileResponse toResponse() {
        return new InstagramProfileResponse(
            id,
            userName,
            name,
            profilePictureUrl,
            followersCount,
            followsCount,
            mediaCount
        );
    }
}