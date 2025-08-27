package site.petful.snsservice.instagram.insight.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
    private InstagramMonthlyId id;

    @Column(nullable = false)
    private Long totalFollowers;

    public InstagramFollowerHistoryEntity(Long instagramId, LocalDate month, Long totalFollowers) {
        this.id = new InstagramMonthlyId(instagramId, month);
        this.totalFollowers = totalFollowers;
    }
}
