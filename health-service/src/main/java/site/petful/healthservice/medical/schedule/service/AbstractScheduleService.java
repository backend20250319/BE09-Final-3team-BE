package site.petful.healthservice.medical.schedule.service;

import site.petful.healthservice.medical.schedule.entity.Schedule;
import site.petful.healthservice.medical.schedule.enums.ScheduleMainType;
import site.petful.healthservice.medical.schedule.enums.ScheduleSubType;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.common.response.ErrorCode;
import site.petful.healthservice.medical.schedule.repository.ScheduleRepository;
import site.petful.healthservice.medical.schedule.dto.ScheduleRequestDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractScheduleService {

    protected final ScheduleRepository scheduleRepository;
    
    // 생성자 추가
    protected AbstractScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    /**
     * 공통 스케줄 엔티티 생성
     */
    protected Schedule createScheduleEntity(Long userNo, ScheduleRequestDTO request, ScheduleMainType mainType) {
        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate() != null ? request.getEndDate() : start;
        LocalTime time = request.getTimes().get(0);
        
        LocalDateTime startDt = LocalDateTime.of(start, time);
        LocalDateTime endDt = LocalDateTime.of(end, time);

        Schedule entity = Schedule.builder()
                .title(request.getTitle())
                .startDate(startDt)
                .endDate(endDt)
                .mainType(mainType)
                .subType(request.getSubType())
                .userNo(userNo)
                .petNo(request.getPetNo())
                .recurrenceType(request.getFrequency())
                .recurrenceInterval(request.getRecurrenceInterval())
                .recurrenceEndDate(endDt)
                .frequency(request.getFrequencyText())
                .times(request.getTimes().stream()
                    .map(LocalTime::toString)
                    .collect(Collectors.joining(",")))
                .build();

        // reminderDaysBefore가 설정되어 있으면 자동으로 알림 활성화
        if (request.getReminderDaysBefore() != null) {
            entity.updateReminders(request.getReminderDaysBefore());
        } else {
            // 기본값으로 당일 알림 활성화
            entity.updateReminders(0);
        }
        entity.ensureDeletedFlag();

        return entity;
    }

    /**
     * 공통 스케줄 엔티티 저장
     */
    protected Long saveSchedule(Schedule schedule) {
        // saveAndFlush를 사용하여 즉시 DB에 반영
        Schedule saved = scheduleRepository.saveAndFlush(schedule);
        
        if (saved.getScheduleNo() == null) {
            throw new BusinessException(ErrorCode.SCHEDULE_SAVE_FAILED, "스케줄 저장 후 ID가 생성되지 않았습니다.");
        }
        
        return saved.getScheduleNo();
    }

    /**
     * 공통 스케줄 엔티티 조회
     */
    protected Schedule findScheduleById(Long scheduleNo) {
        return scheduleRepository.findById(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND, "일정을 찾을 수 없습니다: " + scheduleNo));
    }

    /**
     * 공통 스케줄 엔티티 업데이트
     */
    protected Schedule updateSchedule(Schedule schedule, ScheduleRequestDTO request) {
        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate() != null ? request.getEndDate() : start;
        LocalTime time = request.getTimes().get(0);
        
        LocalDateTime startDt = LocalDateTime.of(start, time);
        LocalDateTime endDt = LocalDateTime.of(end, time);

        schedule.updateSchedule(request.getTitle(), startDt, endDt);
        schedule.updateRecurrence(request.getFrequency(), request.getRecurrenceInterval(), endDt);
        schedule.updateReminders(request.getReminderDaysBefore());
        schedule.updateTimes(request.getTimes());
        schedule.updateSubType(request.getSubType());
        schedule.updatePetNo(request.getPetNo());

        return schedule;
    }

    /**
     * 공통 스케줄 엔티티 삭제
     */
    protected void deleteSchedule(Long scheduleNo) {
        Schedule schedule = findScheduleById(scheduleNo);
        schedule.softDelete();
        scheduleRepository.save(schedule);
    }

    /**
     * 알림 토글
     */
    protected Long toggleAlarm(Long scheduleNo, boolean enabled) {
        Schedule schedule = findScheduleById(scheduleNo);
        
        // 현재 알림 상태 확인
        boolean currentAlarmEnabled = schedule.getReminderDaysBefore() != null;
        
        // 알림을 켜려고 하는데 이미 켜져 있는 경우
        if (enabled && currentAlarmEnabled) {
            throw new BusinessException(ErrorCode.ALARM_ALREADY_ENABLED, "알림이 이미 활성화되어 있습니다.");
        }
        
        // 알림을 끄려고 하는데 이미 꺼져 있는 경우
        if (!enabled && !currentAlarmEnabled) {
            throw new BusinessException(ErrorCode.ALARM_ALREADY_DISABLED, "알림이 이미 비활성화되어 있습니다.");
        }
        
        if (enabled) {
            // 알림 활성화 (기본값: 당일 알림)
            schedule.updateReminders(0);
        } else {
            // 알림 비활성화
            schedule.updateReminders(null);
        }
        
        scheduleRepository.save(schedule);
        return scheduleNo;
    }

    /**
     * 사용자별 일정 조회 (메인타입 필터링)
     */
    protected List<Schedule> findSchedulesByUserAndMainType(Long userNo, ScheduleMainType mainType) {
        return scheduleRepository.findByUserNoAndMainTypeAndDeletedFalseOrderByStartDateAsc(userNo, mainType);
    }

    /**
     * 사용자별 일정 조회 (서브타입 필터링)
     */
    protected List<Schedule> findSchedulesByUserAndSubType(Long userNo, String subType) {
        try {
            ScheduleSubType targetSubType =
                ScheduleSubType.valueOf(subType.toUpperCase());
            return scheduleRepository.findByUserNoAndSubTypeAndDeletedFalseOrderByStartDateAsc(userNo, targetSubType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "유효하지 않은 서브타입입니다: " + subType);
        }
    }
}
