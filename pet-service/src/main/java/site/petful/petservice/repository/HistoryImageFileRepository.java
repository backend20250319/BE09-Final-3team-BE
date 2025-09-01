package site.petful.petservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.petful.petservice.entity.HistoryImageFile;

import java.util.List;

@Repository
public interface HistoryImageFileRepository extends JpaRepository<HistoryImageFile, Long> {
    
    // 특정 활동이력의 모든 이미지 파일 조회 (삭제되지 않은 것만)
    List<HistoryImageFile> findByHistoryNoAndIsDeletedFalse(Long historyNo);
    
    // 특정 활동이력의 모든 이미지 파일 조회 (삭제된 것 포함)
    List<HistoryImageFile> findByHistoryNo(Long historyNo);
    
    // 특정 활동이력의 이미지 파일 개수 조회
    long countByHistoryNoAndIsDeletedFalse(Long historyNo);
    
    // 특정 활동이력의 이미지 파일들을 논리적 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE HistoryImageFile h SET h.isDeleted = true WHERE h.historyNo = :historyNo")
    int softDeleteByHistoryNo(@Param("historyNo") Long historyNo);
}
