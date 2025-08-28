package site.petful.healthservice.activity.repository;

import site.petful.healthservice.activity.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    
    // 특정 날짜에 특정 반려동물의 활동 데이터가 있는지 확인
    boolean existsByPetNoAndActivityDate(Long petNo, LocalDate activityDate);
    
    // 특정 사용자의 모든 반려동물 활동 데이터 조회
    List<Activity> findByUserNoOrderByActivityDateDesc(Long userNo);
    
    // 특정 반려동물의 활동 데이터 조회
    List<Activity> findByPetNoOrderByActivityDateDesc(Long petNo);
    
    // 특정 날짜 범위의 활동 데이터 조회
    @Query("SELECT a FROM Activity a WHERE a.petNo = :petNo AND a.activityDate BETWEEN :startDate AND :endDate ORDER BY a.activityDate DESC")
    List<Activity> findByPetNoAndDateRange(@Param("petNo") Long petNo, 
                                         @Param("startDate") LocalDate startDate, 
                                         @Param("endDate") LocalDate endDate);
    
    // 특정 날짜의 활동 데이터 조회
    Optional<Activity> findByPetNoAndActivityDate(Long petNo, LocalDate activityDate);
}
