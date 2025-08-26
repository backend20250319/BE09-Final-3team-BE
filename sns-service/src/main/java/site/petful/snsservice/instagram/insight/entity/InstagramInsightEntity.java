package site.petful.snsservice.instagram.insight.entity;

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
@Table(name = "instagram_insight")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InstagramInsightEntity {

    @EmbeddedId
    private InstagramMonthlyId id;
    @MapsId("instagramId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instagram_id", nullable = false)
    private InstagramProfileEntity instagramProfile;

    private Long shares;
    private Long likes;
    private Long comments;
    private Long views;
    private Long reach;

}
