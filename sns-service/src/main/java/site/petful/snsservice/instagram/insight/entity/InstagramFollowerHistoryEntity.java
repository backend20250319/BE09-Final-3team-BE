package site.petful.snsservice.instagram.insight.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "instagram_follower_history")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class InstagramFollowerHistoryEntity {

    @EmbeddedId
    private InstagramFollowerHistoryId id;
    @Column(nullable = false)
    private Long totalFollowers;

}
