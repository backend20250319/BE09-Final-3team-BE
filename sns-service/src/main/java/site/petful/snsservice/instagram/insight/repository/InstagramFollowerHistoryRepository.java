package site.petful.snsservice.instagram.insight.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.petful.snsservice.instagram.insight.entity.InstagramFollowerHistoryEntity;
import site.petful.snsservice.instagram.insight.entity.InstagramFollowerHistoryId;

@Repository
public interface InstagramFollowerHistoryRepository extends
    JpaRepository<InstagramFollowerHistoryEntity, InstagramFollowerHistoryId> {

    List<InstagramFollowerHistoryEntity> findById_InstagramId(Long instagramId);

    List<InstagramFollowerHistoryEntity> findById_InstagramIdAndId_MonthAfter(
        Long idInstagramId, LocalDate idStatMonthAfter);
}
