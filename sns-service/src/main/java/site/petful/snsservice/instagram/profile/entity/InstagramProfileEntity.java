package site.petful.snsservice.instagram.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.snsservice.instagram.profile.dto.InstagramProfileDto;

@Entity
@Table(name = "instagram_profile")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InstagramProfileEntity {

    @Id
    private Long id;
    private Long userNo;
    private String username;
    private String name;
    @Column(name = "profile_picture_url", length = 512)
    private String profilePictureUrl;
    private Long followersCount;
    private Long followsCount;
    private Long mediaCount;
    private Boolean autoDelete;

    public InstagramProfileEntity(InstagramProfileDto response, Long user_no, Boolean autoDelete) {
        this.id = response.id();
        this.username = response.username();
        this.name = response.name();
        this.profilePictureUrl = response.profile_picture_url();
        this.followersCount = response.followers_count();
        this.followsCount = response.follows_count();
        this.mediaCount = response.media_count();
        this.userNo = user_no;
        this.autoDelete = autoDelete;
    }

    public void updateFromDto(InstagramProfileDto dto) {
        this.username = dto.username();
        this.name = dto.name();
        this.profilePictureUrl = dto.profile_picture_url();
        this.followersCount = dto.followers_count();
        this.followsCount = dto.follows_count();
        this.mediaCount = dto.media_count();
    }

    public void setAutoDelete(Boolean autoDelete) {
        if (this.autoDelete == autoDelete) {
            throw new IllegalArgumentException(
                "이미 설정된 자동삭제 설정 값입니다.");
        }
        this.autoDelete = autoDelete;
    }
}