package site.petful.snsservice.instagram.comment.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "instagram_banned_word")
@NoArgsConstructor
@AllArgsConstructor
public class InstagramBannedWordEntity {

    @EmbeddedId
    private InstagramBannedWordId id;

    public InstagramBannedWordEntity(Long instagramId, String word) {
        this.id = new InstagramBannedWordId(instagramId, word);
    }

    public String getWord() {
        return id.getWord();
    }

    public Long getInstagramId() {
        return id.getInstagramId();
    }
}
