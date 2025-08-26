package site.petful.snsservice.instagram.insight.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.snsservice.instagram.client.dto.InstagramApiInsightsResponseDto;
import site.petful.snsservice.instagram.client.dto.InstagramApiInsightsResponseDto.InsightData;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;

@Entity
@Table(name = "instagram_insight")
@Getter
@NoArgsConstructor

public class InstagramInsightEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instagram_id", nullable = false)
    private InstagramProfileEntity instagramProfile;

    private Long shares;
    private Long likes;
    private Long comments;
    private Long views;
    private Long reach;
    private LocalDateTime since;
    private LocalDateTime until;

    public InstagramInsightEntity(InstagramApiInsightsResponseDto dto,
        InstagramProfileEntity profile,
        LocalDateTime since, LocalDateTime until) {
        this.instagramProfile = profile;
        for (InsightData insightData : dto.getData()) {
            switch (insightData.getName()) {
                case "shares" -> this.shares = insightData.getTotalValue().getValue();
                case "likes" -> this.likes = insightData.getTotalValue().getValue();
                case "comments" -> this.comments = insightData.getTotalValue().getValue();
                case "views" -> this.views = insightData.getTotalValue().getValue();
                case "reach" -> this.reach = insightData.getTotalValue().getValue();
            }
        }
        this.since = since;
        this.until = until;
    }
}
