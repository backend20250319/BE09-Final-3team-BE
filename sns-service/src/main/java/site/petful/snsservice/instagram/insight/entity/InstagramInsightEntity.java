package site.petful.snsservice.instagram.insight.entity;

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
@Table(name = "instagram_insight",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"month", "instagram_id"})
    })
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InstagramInsightEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instagram_id", nullable = false)
    private InstagramProfileEntity instagramProfile;
    private LocalDate month;


    private Long shares;
    private Long likes;
    private Long comments;
    private Long views;
    private Long reach;

}
