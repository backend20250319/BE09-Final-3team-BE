package site.petful.snsservice.instagram.insight.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.snsservice.instagram.insight.entity.InstagramInsightEntity;
import site.petful.snsservice.instagram.insight.entity.InstagramMonthlyId;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;

public interface InstagramInsightRepository extends
    JpaRepository<InstagramInsightEntity, InstagramMonthlyId> {

    List<InstagramInsightEntity> findByInstagramProfile(InstagramProfileEntity profile);
    
    List<InstagramInsightEntity> findByInstagramProfileAndId_MonthGreaterThanEqual(
        InstagramProfileEntity instagramProfile, LocalDate idMonthIsGreaterThan);
}
