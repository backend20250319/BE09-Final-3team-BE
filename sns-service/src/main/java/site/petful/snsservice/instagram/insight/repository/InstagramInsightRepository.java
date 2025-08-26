package site.petful.snsservice.instagram.insight.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import site.petful.snsservice.instagram.insight.entity.InstagramInsightEntity;
import site.petful.snsservice.instagram.insight.entity.InstagramMonthlyId;

public interface InstagramInsightRepository extends
    JpaRepository<InstagramInsightEntity, InstagramMonthlyId> {

    List<InstagramInsightEntity> findById_InstagramId(Long instagramId);

    List<InstagramInsightEntity> findById_InstagramIdAndId_MonthGreaterThanEqual(
        Long instagramId, LocalDate idMonthIsGreaterThan);
}
