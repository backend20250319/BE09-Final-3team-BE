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
import site.petful.healthservice.medical.dto.CareResponseDTO;
import site.petful.healthservice.medical.dto.CareDetailDTO;
import site.petful.healthservice.medical.dto.CareUpdateRequestDTO;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.common.response.ErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CareScheduleService {

    private final CalendarRepository calendarRepository;

    // ==================== 돌봄 일정 생성 ====================
    
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
            entity.updateReminders(List.of(request.getReminderDaysBefore() == null ? 0 : request.getReminderDaysBefore()));
        } else {
            entity.updateReminders(List.of());
        }

        Calendar saved = calendarRepository.save(entity);
        return saved.getCalNo();
    }

    // ==================== 돌봄 일정 조회 ====================
    
    /**
     * 돌봄 일정 목록 조회
     */
    public List<CareResponseDTO> listCareSchedules(Long userNo, String from, String to, String subType) {
        LocalDateTime start;
        LocalDateTime end;
        try {
            start = (from == null || from.isBlank())
                    ? LocalDate.now().minusMonths(1).atStartOfDay()
                    : LocalDate.parse(from).atStartOfDay();
            end = (to == null || to.isBlank())
                    ? LocalDate.now().plusMonths(1).atTime(23, 59, 59)
                    : LocalDate.parse(to).atTime(23, 59, 59);
        } catch (java.time.format.DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_DATE_FORMAT, "유효하지 않은 날짜 형식입니다.");
        }
        if (start.isAfter(end)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE, "from이 to보다 늦을 수 없습니다.");
        }

        List<Calendar> items = calendarRepository.findByUserNoAndDateRange(userNo, start, end);

        var stream = items.stream()
                .filter(c -> c.getMainType() == CalendarMainType.CARE);
        if (subType != null && !subType.isBlank()) {
            stream = stream.filter(c -> c.getSubType().name().equalsIgnoreCase(subType));
        }

        return stream
                .map(c -> CareResponseDTO.builder()
                        .calNo(c.getCalNo())
                        .title(c.getTitle())
                        .startDate(c.getStartDate())
                        .endDate(c.getEndDate())
                        .mainType(c.getMainType().name())
                        .subType(c.getSubType().name())
                        .frequency(c.getFrequency())
                        .alarmEnabled(c.getReminderDaysBefore() != null && !c.getReminderDaysBefore().isEmpty())
                        .reminderDaysBefore((c.getReminderDaysBefore() == null || c.getReminderDaysBefore().isEmpty()) ? null : c.getReminderDaysBefore().get(0))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 돌봄 일정 상세 조회
     */
    public CareDetailDTO getCareDetail(Long calNo, Long userNo) {
        Calendar c = calendarRepository.findById(calNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "일정을 찾을 수 없습니다."));
        
        if (!c.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }
        if (Boolean.TRUE.equals(c.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "삭제된 일정입니다.");
        }
        if (c.getMainType() != CalendarMainType.CARE) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "돌봄 일정이 아닙니다.");
        }

        return CareDetailDTO.builder()
                .calNo(c.getCalNo())
                .title(c.getTitle())
                .mainType(c.getMainType().name())
                .subType(c.getSubType().name())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .time(c.getStartDate() != null ? c.getStartDate().toLocalTime() : null)
                .frequency(c.getFrequency())
                .alarmEnabled(c.getReminderDaysBefore() != null && !c.getReminderDaysBefore().isEmpty())
                .reminderDaysBefore(c.getReminderDaysBefore())
                .build();
    }

    // ==================== 돌봄 일정 수정 ====================
    
    /**
     * 돌봄 일정 수정 (부분 업데이트)
     */
    public Long updateCareSchedule(Long calNo, CareUpdateRequestDTO request, Long userNo) {
        // 조회 및 소유자 검증
        Calendar entity = calendarRepository.findById(calNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "일정을 찾을 수 없습니다."));
        
        if (!entity.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }
        
        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "삭제된 일정입니다.");
        }
        
        if (entity.getMainType() != CalendarMainType.CARE) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "돌봄 일정이 아닙니다.");
        }

        // 일정 업데이트
        updateCareScheduleFields(entity, request);
        
        // 엔티티 저장
        calendarRepository.save(entity);
        return entity.getCalNo();
    }

    private void updateCareScheduleFields(Calendar entity, CareUpdateRequestDTO request) {
        // 기본 정보 업데이트
        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        
        if (request.getSubType() != null) {
            entity.updateSubType(request.getSubType());
        }

        // 날짜/시간 업데이트
        LocalDate base = request.getStartDate() != null ? request.getStartDate() : entity.getStartDate().toLocalDate();
        LocalDate endBase = request.getEndDate() != null ? request.getEndDate() : entity.getEndDate().toLocalDate();
        LocalTime time = request.getTime() != null ? request.getTime() : entity.getStartDate().toLocalTime();

        LocalDateTime startDt = LocalDateTime.of(base, time);
        LocalDateTime endDt = LocalDateTime.of(endBase, time);

        entity.updateSchedule(entity.getTitle(), startDt, endDt, startDt);

        // 빈도/반복 업데이트
        if (request.getFrequency() != null) {
            CareFrequency cf = request.getFrequency();
            RecurrenceType recurrenceType = cf.getRecurrenceType();
            Integer interval = cf.getInterval();
            
            entity.updateFrequency(cf.getLabel());
            entity.updateRecurrence(recurrenceType, interval, endDt);
        }

        // 알림 처리
        if (request.getAlarmEnabled() != null) {
            if (!request.getAlarmEnabled()) {
                entity.updateReminders(List.of());
            } else if (request.getReminderDaysBefore() != null) {
                entity.updateReminders(List.of(request.getReminderDaysBefore()));
            }
        } else if (request.getReminderDaysBefore() != null) {
            entity.updateReminders(List.of(request.getReminderDaysBefore()));
        }
    }

    // ==================== 돌봄 일정 삭제 ====================
    
    /**
     * 돌봄 일정 삭제 (soft delete)
     */
    public Long deleteCareSchedule(Long calNo, Long userNo) {
        Calendar entity = calendarRepository.findById(calNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "일정을 찾을 수 없습니다."));

        if (!entity.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }

        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "이미 삭제된 일정입니다.");
        }

        entity.softDelete();
        calendarRepository.save(entity);
        return calNo;
    }

    // ==================== 메타 정보 조회 ====================
    
    /**
     * 돌봄 관련 메타 정보 조회 (드롭다운용)
     */
    public java.util.Map<String, java.util.List<String>> getCareMeta() {
        java.util.List<String> subTypes = java.util.Arrays.stream(CalendarSubType.values())
                .filter(CalendarSubType::isCareType)
                .map(Enum::name)
                .toList();
        java.util.List<String> frequencies = java.util.Arrays.stream(CareFrequency.values())
                .map(Enum::name)
                .toList();
        java.util.Map<String, java.util.List<String>> data = new java.util.HashMap<>();
        data.put("subTypes", subTypes);
        data.put("frequencies", frequencies);
        return data;
    }
}


