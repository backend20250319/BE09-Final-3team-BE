package site.petful.snsservice.instagram.insight.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.snsservice.instagram.insight.entity.InstagramFollowerHistoryEntity;
import site.petful.snsservice.instagram.insight.entity.InstagramMonthlyId;
import site.petful.snsservice.instagram.profile.entity.InstagramProfileEntity;

@Repository
public interface InstagramFollowerHistoryRepository extends
    JpaRepository<InstagramFollowerHistoryEntity, InstagramMonthlyId> {

    List<InstagramFollowerHistoryEntity> findByProfile(InstagramProfileEntity profile);

    List<InstagramFollowerHistoryEntity> findByProfileAndId_MonthGreaterThanEqual(
        InstagramProfileEntity instagramProfile, LocalDate monthGreaterThanEqual);
}
