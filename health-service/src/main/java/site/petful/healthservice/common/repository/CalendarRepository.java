package site.petful.healthservice.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.petful.healthservice.common.entity.Calendar;
import site.petful.healthservice.common.enums.CalendarMainType;
import site.petful.healthservice.common.enums.CalendarSubType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Long> {

    // 사용자별 일정 조회
    List<Calendar> findByUserNoAndDeletedFalseOrderByStartDateAsc(Long userNo);

    // 특정 기간 일정 조회
    @Query("SELECT c FROM Calendar c WHERE c.userNo = :userNo " +
           "AND c.deleted = false " +
           "AND c.startDate >= :startDate AND c.startDate <= :endDate " +
           "ORDER BY c.startDate ASC")
    List<Calendar> findByUserNoAndDateRange(@Param("userNo") Long userNo,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    // 메인 타입별 조회
    List<Calendar> findByUserNoAndMainTypeAndDeletedFalseOrderByStartDateAsc(Long userNo, CalendarMainType mainType);

    // 서브 타입별 조회
    List<Calendar> findByUserNoAndSubTypeAndDeletedFalseOrderByStartDateAsc(Long userNo, CalendarSubType subType);

    // 투약 일정만 조회 (알림용)
    @Query("SELECT c FROM Calendar c WHERE c.userNo = :userNo " +
           "AND c.deleted = false " +
           "AND c.mainType = 'MEDICATION' " +
           "AND c.alarmTime BETWEEN :startTime AND :endTime " +
           "ORDER BY c.alarmTime ASC")
    List<Calendar> findUpcomingMedicationAlarms(@Param("userNo") Long userNo,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    // 특정 약물명으로 일정 조회 - 캘린더에 약물명이 없어져서 비활성화 (상세 테이블에서 처리 권장)
}