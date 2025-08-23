package site.petful.snsservice.instagram.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import site.petful.snsservice.instagram.client.dto.InstagramProfileResponseDto;

@Entity
@Table(name = "instagram_profile")
@NoArgsConstructor
@AllArgsConstructor
public class InstagramProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    private Long userNo;
    private String userName;
    private String name;
    @Column(name = "profile_picture_url", length = 512)
    private String profilePictureUrl;
    private Integer followersCount;
    private Integer followsCount;
    private Integer mediaCount;


    public InstagramProfileEntity(InstagramProfileResponseDto response, Long user_no) {
        this.id = response.id();
        this.userName = response.username();
        this.name = response.name();
        this.profilePictureUrl = response.profile_picture_url();
        this.followersCount = response.followers_count();
        this.followsCount = response.follows_count();
        this.mediaCount = response.media_count();
        this.userNo = user_no;
    }

    public InstagramProfileResponseDto toInstagramProfileDto() {
        return new InstagramProfileResponseDto(
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