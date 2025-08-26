package site.petful.snsservice.instagram.comment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false)
    String word;
    @ManyToOne
    @JoinColumn(name = "instagram_profile_id", nullable = false)
    InstagramProfileEntity instagramProfile;

    public InstagramBannedWordEntity(String word, InstagramProfileEntity instagramProfile) {
        this.word = word;
        this.instagramProfile = instagramProfile;
    }
}
