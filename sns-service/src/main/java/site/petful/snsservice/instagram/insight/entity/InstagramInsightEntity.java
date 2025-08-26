package site.petful.snsservice.instagram.insight.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "instagram_insight")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InstagramInsightEntity {

    @EmbeddedId
    private InstagramMonthlyId id;
    private Long shares;
    private Long likes;
    private Long comments;
    private Long views;
    private Long reach;

}
