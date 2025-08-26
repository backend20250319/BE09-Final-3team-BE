package site.petful.healthservice.medical.care.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.healthservice.common.entity.Calendar;
import site.petful.healthservice.common.enums.CalendarMainType;
import site.petful.healthservice.common.enums.CalendarSubType;
import site.petful.healthservice.common.enums.RecurrenceType;
import site.petful.healthservice.common.enums.CareFrequency;
import site.petful.healthservice.common.repository.CalendarRepository;
import site.petful.healthservice.medical.care.dto.CareRequestDTO;
import site.petful.healthservice.medical.care.dto.CareResponseDTO;
import site.petful.healthservice.medical.care.dto.CareDetailDTO;
import site.petful.healthservice.medical.care.dto.CareUpdateRequestDTO;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.common.response.ErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CareScheduleService {

    private final CalendarRepository calendarRepository;

    // ==================== 돌봄 일정 생성 ====================
    
    public Long createCareSchedule(Long userNo, @Valid CareRequestDTO request) {
        CareFrequency cf = request.getFrequency();
        RecurrenceType recurrenceType = cf.getRecurrenceType();
        Integer interval = cf.getInterval();

        CalendarSubType subType = request.getSubType();
        
        CalendarMainType mainType;
        if (subType.isVaccinationType()) {
            mainType = CalendarMainType.VACCINATION;
        } else {
            mainType = CalendarMainType.CARE;
        }

        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate();
        LocalTime time = request.getTimes().get(0);
        LocalDateTime startDt = LocalDateTime.of(start, time);
        LocalDateTime endDt = LocalDateTime.of(end, time);

        Calendar entity = Calendar.builder()
                .title(request.getTitle())
                .startDate(startDt)
                .endDate(endDt)
                .mainType(mainType)
                .subType(subType)
                .allDay(false)
                .alarmTime(startDt)
                .userNo(userNo)
                .recurrenceType(recurrenceType)
                .recurrenceInterval(interval)
                .recurrenceEndDate(endDt)
                .frequency(cf.getLabel())
                .times(List.of(time))
                .build();

        entity.updateReminders(List.of(request.getReminderDaysBefore()));

        Calendar saved = calendarRepository.save(entity);
        return saved.getCalNo();
    }

    // ==================== 돌봄 일정 조회 ====================
    
    /**
     * 돌봄 일정 목록 조회
     */
    public List<CareResponseDTO> listCareSchedules(Long userNo, String from, String to, String subType) {
        List<Calendar> items;
        
        // 날짜 범위가 지정된 경우에만 날짜 필터링 적용
        if (from != null && !from.isBlank() && to != null && !to.isBlank()) {
            LocalDateTime start;
            LocalDateTime end;
            try {
                start = LocalDate.parse(from).atStartOfDay();
                end = LocalDate.parse(to).atTime(23, 59, 59);
            } catch (java.time.format.DateTimeParseException e) {
                throw new BusinessException(ErrorCode.INVALID_DATE_FORMAT, "유효하지 않은 날짜 형식입니다.");
            }
            if (start.isAfter(end)) {
                throw new BusinessException(ErrorCode.INVALID_DATE_RANGE, "from이 to보다 늦을 수 없습니다.");
            }
            items = calendarRepository.findByUserNoAndDateRange(userNo, start, end);
        } else {
            // 날짜 범위가 없으면 전체 일정 조회
            items = calendarRepository.findByUserNoAndDeletedFalseOrderByStartDateAsc(userNo);
        }

        var stream = items.stream()
                .filter(c -> c.getMainType() == CalendarMainType.CARE || c.getMainType() == CalendarMainType.VACCINATION);
        
        if (subType != null && !subType.isBlank()) {
            try {
                CalendarSubType targetSubType = CalendarSubType.valueOf(subType.toUpperCase());
                
                if (targetSubType.isVaccinationType()) {
                    // 접종 관련 서브타입이면 VACCINATION 메인타입만
                    stream = stream.filter(c -> c.getMainType() == CalendarMainType.VACCINATION);
                } else {
                    // 일반 돌봄 서브타입이면 CARE 메인타입만
                    stream = stream.filter(c -> c.getMainType() == CalendarMainType.CARE);
                }
                
                // 서브타입도 정확히 매칭
                stream = stream.filter(c -> c.getSubType() == targetSubType);
                
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "유효하지 않은 서브타입입니다: " + subType);
            }
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
                        .times(c.getTimes())
                        .build())
                .toList();
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
        if (c.getMainType() != CalendarMainType.CARE && c.getMainType() != CalendarMainType.VACCINATION) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "돌봄 또는 접종 일정이 아닙니다.");
        }

        return CareDetailDTO.builder()
                .calNo(c.getCalNo())
                .title(c.getTitle())
                .mainType(c.getMainType().name())
                .subType(c.getSubType().name())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .times(c.getTimes())
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
        
        if (entity.getMainType() != CalendarMainType.CARE && entity.getMainType() != CalendarMainType.VACCINATION) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "돌봄 또는 접종 일정이 아닙니다.");
        }
        
        // 메인타입 변경 방지
        if (request.getSubType() != null) {
            CalendarSubType newSubType = request.getSubType();
            if (entity.getMainType() == CalendarMainType.CARE && newSubType.isVaccinationType()) {
                throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "돌봄 일정을 접종 일정으로 변경할 수 없습니다.");
            }
            if (entity.getMainType() == CalendarMainType.VACCINATION && !newSubType.isVaccinationType()) {
                throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "접종 일정을 돌봄 일정으로 변경할 수 없습니다.");
            }
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
            entity.updateSchedule(request.getTitle(), entity.getStartDate(), entity.getEndDate(), entity.getAlarmTime());
        }
        
        if (request.getSubType() != null) {
            entity.updateSubType(request.getSubType());
        }

        // times 필드 업데이트
        if (request.getTimes() != null && !request.getTimes().isEmpty()) {
            entity.updateTimes(request.getTimes());
        }

        // 날짜/시간 업데이트
        LocalDate base = request.getStartDate() != null ? request.getStartDate() : entity.getStartDate().toLocalDate();
        LocalDate endBase = request.getEndDate() != null ? request.getEndDate() : entity.getEndDate().toLocalDate();
        LocalTime time = (request.getTimes() != null && !request.getTimes().isEmpty()) 
            ? request.getTimes().get(0) 
            : entity.getStartDate().toLocalTime();

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

    // ==================== 알림 관리 ====================
    
    /**
     * 알림 활성화/비활성화 토글
     */
    public Boolean toggleAlarm(Long calNo, Long userNo) {
        Calendar entity = calendarRepository.findById(calNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "일정을 찾을 수 없습니다."));

        if (!entity.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }

        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "삭제된 일정입니다.");
        }

        if (entity.getMainType() != CalendarMainType.CARE && entity.getMainType() != CalendarMainType.VACCINATION) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "돌봄 또는 접종 일정이 아닙니다.");
        }

        // 현재 알림 상태를 반대로 토글
        boolean currentAlarmEnabled = entity.getReminderDaysBefore() != null && !entity.getReminderDaysBefore().isEmpty();
        if (currentAlarmEnabled) {
            // 알림 비활성화
            entity.updateReminders(List.of());
        } else {
            // 알림 활성화 (기본값: 당일 알림)
            entity.updateReminders(List.of(0));
        }

        calendarRepository.save(entity);
        return !currentAlarmEnabled; // 새로운 알림 상태 반환
    }



    // ==================== 메타 정보 조회 ====================
    
    /**
     * 돌봄 및 접종 관련 메타 정보 조회 (드롭다운용)
     */
    public java.util.Map<String, java.util.List<String>> getCareMeta() {
        java.util.List<String> subTypes = java.util.Arrays.stream(CalendarSubType.values())
                .filter(st -> st.isCareType() || st.isVaccinationType())
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


