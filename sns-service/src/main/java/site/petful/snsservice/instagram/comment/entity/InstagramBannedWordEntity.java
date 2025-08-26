package site.petful.snsservice.instagram.comment.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;

@Entity
@Table(name = "instagram_banned_word")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InstagramBannedWordEntity {

    @EmbeddedId
    private InstagramBannedWordId instagramBannedWordId;

    @MapsId("instagramId")
    @ManyToOne
    @JoinColumn(name = "instagram_profile_id", nullable = false)
    private InstagramProfileEntity instagramProfile;

    public String getWord() {
        return instagramBannedWordId.getWord();
    }
}
