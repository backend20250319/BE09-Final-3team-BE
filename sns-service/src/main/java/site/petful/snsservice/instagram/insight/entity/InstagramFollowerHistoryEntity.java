package site.petful.snsservice.instagram.insight.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;

@Entity
@Table(name = "instagram_follower_history",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"month", "instagram_id"})
    })
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class InstagramFollowerHistoryEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instagram_id", nullable = false)
    private InstagramProfileEntity instagramProfile;
    private LocalDate month;

    @Column(nullable = false)
    private Long totalFollowers;

}
