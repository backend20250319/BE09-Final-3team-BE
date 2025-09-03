package site.petful.petservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.petful.petservice.entity.PortfolioImageFile;

import java.util.List;

@Repository
public interface PortfolioImageFileRepository extends JpaRepository<PortfolioImageFile, Long> {

    // 특정 활동이력의 모든 이미지 파일 조회 (삭제되지 않은 것만)
    List<PortfolioImageFile> findByHistoryNoAndIsDeletedFalse(Long historyNo);

    // 특정 활동이력의 모든 이미지 파일 조회 (삭제된 것 포함)
    List<PortfolioImageFile> findByHistoryNo(Long historyNo);

    // 특정 반려동물의 모든 활동이력 이미지 조회
    @Query("SELECT pif FROM PortfolioImageFile pif JOIN History h ON pif.historyNo = h.historyNo WHERE h.petNo = :petNo AND pif.isDeleted = false")
    List<PortfolioImageFile> findByPetNo(@Param("petNo") Long petNo);

    // 파일명으로 조회
    PortfolioImageFile findBySavedNameAndIsDeletedFalse(String savedName);

    // 특정 활동이력의 이미지 개수 조회
    long countByHistoryNoAndIsDeletedFalse(Long historyNo);

    // 특정 반려동물의 모든 활동이력 이미지 개수 조회
    @Query("SELECT COUNT(pif) FROM PortfolioImageFile pif JOIN History h ON pif.historyNo = h.historyNo WHERE h.petNo = :petNo AND pif.isDeleted = false")
    long countByPetNo(@Param("petNo") Long petNo);
}
