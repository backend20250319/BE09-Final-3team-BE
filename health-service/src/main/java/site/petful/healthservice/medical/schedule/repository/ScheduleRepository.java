package site.petful.healthservice.medical.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.petful.healthservice.medical.schedule.entity.Schedule;
import site.petful.healthservice.medical.schedule.enums.ScheduleMainType;
import site.petful.healthservice.medical.schedule.enums.ScheduleSubType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 사용자별 일정 조회
    List<Schedule> findByUserNoAndDeletedFalseOrderByStartDateAsc(Long userNo);

    // 특정 기간 일정 조회
    @Query("SELECT s FROM Schedule s WHERE s.userNo = :userNo " +
           "AND s.deleted = false " +
           "AND s.startDate >= :startDate AND s.startDate <= :endDate " +
           "ORDER BY s.startDate ASC")
    List<Schedule> findByUserNoAndDateRange(@Param("userNo") Long userNo,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    // 메인 타입별 조회
    List<Schedule> findByUserNoAndMainTypeAndDeletedFalseOrderByStartDateAsc(Long userNo, ScheduleMainType mainType);

    // 서브 타입별 조회
    List<Schedule> findByUserNoAndSubTypeAndDeletedFalseOrderByStartDateAsc(Long userNo, ScheduleSubType subType);

    // 투약 일정만 조회 (알림용)
    @Query("SELECT s FROM Schedule s WHERE s.userNo = :userNo " +
           "AND s.mainType = 'MEDICATION' " +
           "AND s.deleted = false " +
           "AND s.startDate >= :startDate " +
           "ORDER BY s.startDate ASC")
    List<Schedule> findMedicationSchedulesForAlarm(@Param("userNo") Long userNo,
                                                  @Param("startDate") LocalDateTime startDate);

    // 투약 일정 조회 (JOIN FETCH로 N+1 문제 해결)
    @Query("SELECT s FROM Schedule s " +
           "WHERE s.userNo = :userNo " +
           "AND s.deleted = false " +
           "AND s.startDate >= :startDate " +
           "AND s.startDate <= :endDate " +
           "ORDER BY s.startDate ASC")
    List<Schedule> findSchedulesWithMedicationDetails(@Param("userNo") Long userNo,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);
}
