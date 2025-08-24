package site.petful.healthservice.medical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.healthservice.common.entity.Calendar;
import site.petful.healthservice.common.enums.CalendarMainType;
import site.petful.healthservice.common.enums.CalendarSubType;
import site.petful.healthservice.common.enums.RecurrenceType;
import site.petful.healthservice.common.enums.CareFrequency;
import site.petful.healthservice.common.repository.CalendarRepository;
import site.petful.healthservice.medical.dto.CareRequestDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CareScheduleService {

    private final CalendarRepository calendarRepository;

    public CalendarRepository getCalendarRepository() { return calendarRepository; }

    public Long createCareSchedule(Long userNo, CareRequestDTO request) {
        // 빈도 → 반복 설정 매핑 (enum 기반)
        CareFrequency cf = request.getFrequency();
        RecurrenceType recurrenceType = cf != null ? cf.getRecurrenceType() : RecurrenceType.DAILY;
        Integer interval = cf != null ? cf.getInterval() : 1;

        CalendarSubType subType = request.getSubType() != null ? request.getSubType() : CalendarSubType.ETC;

        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate();
        LocalDateTime startDt = LocalDateTime.of(start, request.getTime());
        LocalDateTime endDt = LocalDateTime.of(end, request.getTime());

        Calendar entity = Calendar.builder()
                .title(request.getTitle())
                .startDate(startDt)
                .endDate(endDt)
                .mainType(CalendarMainType.CARE)
                .subType(subType)
                .allDay(false)
                .description(null)
                .alarmTime(startDt)
                .userNo(userNo)
                .recurrenceType(recurrenceType)
                .recurrenceInterval(interval)
                .recurrenceEndDate(endDt)
                // 공통 칼럼에 빈도 라벨 저장 (조회/통계 용도)
                .frequency(cf != null ? cf.getLabel() : CareFrequency.DAILY.getLabel())
                .build();

        // 알림 설정
        if (request.getAlarmEnabled() == null || request.getAlarmEnabled()) {
            entity.updateReminders(java.util.List.of(request.getReminderDaysBefore() == null ? 0 : request.getReminderDaysBefore()));
        } else {
            entity.updateReminders(java.util.List.of());
        }

        Calendar saved = calendarRepository.save(entity);
        return saved.getCalNo();
    }

    
}


