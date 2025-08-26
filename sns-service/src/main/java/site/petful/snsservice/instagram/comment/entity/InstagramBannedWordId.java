package site.petful.snsservice.instagram.comment.entity;


import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class InstagramBannedWordId implements Serializable {

    private Long instagramId;
    private String word;

}
