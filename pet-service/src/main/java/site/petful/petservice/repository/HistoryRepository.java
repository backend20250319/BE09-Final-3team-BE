package site.petful.petservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.petful.petservice.entity.History;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    
    // 펫 번호로 활동 이력 목록 조회 (기본)
    List<History> findByPetNo(Long petNo);
    
    // 펫 번호로 활동 이력 목록 조회 (시작일 기준 내림차순)
    List<History> findByPetNoOrderByHistoryStartDesc(Long petNo);
    
    // 펫 번호로 활동 이력 삭제
    void deleteByPetNo(Long petNo);
    
    // 특정 기간의 활동 이력 조회
    @Query("SELECT h FROM History h WHERE h.petNo = :petNo AND h.historyStart >= :startDate AND h.historyEnd <= :endDate ORDER BY h.historyStart DESC")
    List<History> findByPetNoAndDateRange(@Param("petNo") Long petNo, 
                                         @Param("startDate") String startDate, 
                                         @Param("endDate") String endDate);
}
