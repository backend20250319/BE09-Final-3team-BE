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
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false)
    private Long userNo;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String name;
    @Column(name = "profile_picture_url", length = 512, nullable = false)
    private String profilePictureUrl;
    @Column(nullable = false)
    private Integer followersCount;
    @Column(nullable = false)
    private Integer followsCount;
    @Column(nullable = false)
    private Integer mediaCount;
    @Column(nullable = false)
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

    public void setAutoDelete(Boolean autoDelete) {
        if (this.autoDelete == autoDelete) {
            throw new IllegalArgumentException(
                "이미 설정된 자동삭제 설정 값입니다.");
        }
        this.autoDelete = autoDelete;
    }
}