package site.petful.snsservice.instagram.insight.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;

@Entity
@Table(name = "instagram_follower_history")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class InstagramFollowerHistoryEntity {


    @EmbeddedId
    private InstagramMonthlyId id;
    @MapsId("instagramId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instagram_id", nullable = false)
    private InstagramProfileEntity profile;

    @Column(nullable = false)
    private Long totalFollowers;
}
