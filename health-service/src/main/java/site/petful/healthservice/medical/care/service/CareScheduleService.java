package site.petful.healthservice.medical.care.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.petful.healthservice.medical.schedule.entity.Schedule;
import site.petful.healthservice.medical.schedule.enums.ScheduleMainType;
import site.petful.healthservice.medical.schedule.enums.ScheduleSubType;
import site.petful.healthservice.medical.schedule.enums.RecurrenceType;
import site.petful.healthservice.medical.care.enums.CareFrequency;
import site.petful.healthservice.medical.care.dto.CareRequestDTO;
import site.petful.healthservice.medical.care.dto.CareResponseDTO;
import site.petful.healthservice.medical.care.dto.CareDetailDTO;
import site.petful.healthservice.medical.care.dto.CareUpdateRequestDTO;
import site.petful.healthservice.medical.schedule.repository.ScheduleRepository;
import site.petful.healthservice.medical.schedule.service.AbstractScheduleService;
import site.petful.healthservice.medical.schedule.dto.ScheduleRequestDTO;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.common.response.ErrorCode;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.client.PetServiceClient;
import site.petful.healthservice.common.dto.PetResponse;
import site.petful.healthservice.medical.medication.entity.ScheduleMedDetail;
import site.petful.healthservice.medical.medication.repository.ScheduleMedicationDetailRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import site.petful.healthservice.connectNotice.dto.EventMessage;

@Slf4j
@Service
public class CareScheduleService extends AbstractScheduleService {

    private final PetServiceClient petServiceClient;
    private final RabbitTemplate rabbitTemplate;
    private final ScheduleMedicationDetailRepository medicationDetailRepository;

    public CareScheduleService(ScheduleRepository scheduleRepository, PetServiceClient petServiceClient, 
                             RabbitTemplate rabbitTemplate, ScheduleMedicationDetailRepository medicationDetailRepository) {
        super(scheduleRepository);
        this.petServiceClient = petServiceClient;
        this.rabbitTemplate = rabbitTemplate;
        this.medicationDetailRepository = medicationDetailRepository;
    }

    // ==================== 돌봄 일정 생성 ====================
    
    public Long createCareSchedule(Long userNo, @Valid CareRequestDTO request) {
        // careFrequency 처리
        CareFrequency careFreq = request.getCareFrequency() != null ? request.getCareFrequency() : CareFrequency.DAILY;
        
        // 빈도별 날짜 검증 및 종료일 자동 계산
        LocalDate calculatedEndDate = validateAndCalculateEndDate(request.getStartDate(), request.getEndDate(), careFreq);
        
        ScheduleSubType subType = request.getSubType();
        ScheduleMainType mainType = ScheduleMainType.CARE;

        RecurrenceType recurrenceType = careFreq.getRecurrenceType();
        Integer interval = careFreq.getInterval();
        String frequencyText = careFreq.getLabel();

        // 공통 DTO로 변환
        ScheduleRequestDTO commonRequest = ScheduleRequestDTO.builder()
                .petNo(request.getPetNo())
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(calculatedEndDate)
                .subType(request.getSubType())
                .times(request.getTimes())
                .frequency(recurrenceType)
                .recurrenceInterval(interval)
                .recurrenceEndDate(calculatedEndDate)
                .reminderDaysBefore(request.getReminderDaysBefore())
                .frequencyText(frequencyText)
                .build();

        // 공통 서비스 사용
        Schedule entity = createScheduleEntity(userNo, commonRequest, mainType);
        Long scheduleNo = saveSchedule(entity);
        
        // 스케줄 생성 이벤트 발행
        publishScheduleCreatedEvent(entity);
        
        return scheduleNo;
    }

    // ==================== 돌봄 일정 조회 ====================
    
    /**
     * 돌봄 일정 목록 조회
     */
    public List<CareResponseDTO> listCareSchedules(Long userNo, Long petNo, String from, String to, String subType) {


        List<Schedule> items;
        
        // 날짜 범위가 지정된 경우에만 날짜 필터링 적용
        if (from != null && !from.isBlank() && to != null && !to.isBlank()) {
            LocalDateTime start;
            LocalDateTime end;
            try {
                start = LocalDate.parse(from).atStartOfDay();
                end = LocalDate.parse(to).atTime(23, 59, 59);
            } catch (java.time.format.DateTimeParseException e) {
                throw new BusinessException(ErrorCode.MEDICAL_DATE_FORMAT_ERROR, "건강관리 일정의 날짜 형식이 올바르지 않습니다.");
            }
            if (start.isAfter(end)) {
                throw new BusinessException(ErrorCode.MEDICAL_DATE_RANGE_ERROR, "건강관리 일정의 날짜 범위가 올바르지 않습니다.");
            }
            items = scheduleRepository.findByUserNoAndDateRange(userNo, start, end);
        } else {
            // 날짜 범위가 없으면 전체 일정 조회
            items = scheduleRepository.findByUserNoAndDeletedFalseOrderByStartDateAsc(userNo);
        }

        var stream = items.stream()
                .filter(c -> c.getMainType() == ScheduleMainType.CARE)
                .filter(c -> c.getPetNo().equals(petNo)); // 특정 펫의 일정만 필터링
        
        if (subType != null && !subType.isBlank()) {
            try {
                ScheduleSubType targetSubType = ScheduleSubType.valueOf(subType.toUpperCase());
                
                // 모든 서브타입이 CARE 메인타입이므로 메인타입 필터링 제거
                
                // 서브타입도 정확히 매칭
                stream = stream.filter(c -> c.getSubType() == targetSubType);
                
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "유효하지 않은 서브타입입니다: " + subType);
            }
        }

        return stream
                .map(c -> CareResponseDTO.builder()
                        .scheduleNo(c.getScheduleNo())
                        .title(c.getTitle())
                        .startDate(c.getStartDate())
                        .endDate(c.getEndDate())
                        .mainType(c.getMainType().name())
                        .subType(c.getSubType().name())
                        .frequency(c.getFrequency())
                        .alarmEnabled(c.getReminderDaysBefore() != null)
                        .reminderDaysBefore(c.getReminderDaysBefore())
                        .lastReminderDaysBefore(c.getLastReminderDaysBefore())
                        .times(c.getTimesAsList())
                        .build())
                .toList();
    }

    /**
     * 돌봄 일정 상세 조회
     */
    public CareDetailDTO getCareDetail(Long calNo, Long userNo) {
        Schedule c = findScheduleById(calNo);
        

        
        if (!c.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }
        if (Boolean.TRUE.equals(c.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "삭제된 일정입니다.");
        }
        if (c.getMainType() != ScheduleMainType.CARE && c.getMainType() != ScheduleMainType.VACCINATION) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "돌봄 또는 접종 일정이 아닙니다.");
        }

        return CareDetailDTO.builder()
                .scheduleNo(c.getScheduleNo())
                .title(c.getTitle())
                .mainType(c.getMainType().name())
                .subType(c.getSubType().name())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .times(c.getTimesAsList())
                .frequency(c.getFrequency())
                .alarmEnabled(c.getReminderDaysBefore() != null)
                .reminderDaysBefore(c.getReminderDaysBefore())
                .lastReminderDaysBefore(c.getLastReminderDaysBefore())
                .build();
    }

    // ==================== 돌봄 일정 수정 ====================
    
    /**
     * 돌봄 일정 수정 (부분 업데이트)
     */
    public Long updateCareSchedule(Long calNo, CareUpdateRequestDTO request, Long userNo) {
        // 조회 및 소유자 검증
        Schedule entity = findScheduleById(calNo);
        

        
        if (!entity.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }
        
        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "삭제된 일정입니다.");
        }
        
        if (entity.getMainType() != ScheduleMainType.CARE) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "돌봄 일정이 아닙니다.");
        }
        
        // 메인타입 변경 방지 로직 제거 (모든 서브타입이 CARE 메인타입이므로)

        // 날짜 검증 및 종료일 자동 계산
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : entity.getStartDate().toLocalDate();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : entity.getEndDate().toLocalDate();
        
        // 수정 시에는 기존 빈도 정보를 사용하거나 요청에서 받은 빈도 사용
        CareFrequency frequency = request.getCareFrequency() != null ? request.getCareFrequency() : 
            (entity.getFrequency() != null ? CareFrequency.from(entity.getFrequency()) : CareFrequency.DAILY);
        
        LocalDate calculatedEndDate = validateAndCalculateEndDate(startDate, endDate, frequency);

        // 일정 업데이트 (계산된 종료일 포함)
        updateCareScheduleFields(entity, request, calculatedEndDate);
        
        // 엔티티 저장
        scheduleRepository.save(entity);
        return entity.getScheduleNo();
    }

    private void updateCareScheduleFields(Schedule entity, CareUpdateRequestDTO request, LocalDate calculatedEndDate) {
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

        // 날짜/시간 업데이트 (계산된 종료일 사용)
        LocalDate base = request.getStartDate() != null ? request.getStartDate() : entity.getStartDate().toLocalDate();
        LocalTime time = (request.getTimes() != null && !request.getTimes().isEmpty()) 
            ? request.getTimes().get(0) 
            : entity.getStartDate().toLocalTime();

        LocalDateTime startDt = LocalDateTime.of(base, time);
        LocalDateTime endDt = LocalDateTime.of(calculatedEndDate, time);

        entity.updateSchedule(entity.getTitle(), startDt, endDt, startDt);

        // 빈도/반복 업데이트
        if (request.getCareFrequency() != null) {
            CareFrequency cf = request.getCareFrequency();
            RecurrenceType recurrenceType = cf.getRecurrenceType();
            Integer interval = cf.getInterval();
            
            entity.updateFrequency(cf.getLabel());
            entity.updateRecurrence(recurrenceType, interval, endDt);
        }

        // 알림 처리
        if (request.getAlarmEnabled() != null) {
            if (!request.getAlarmEnabled()) {
                entity.updateReminders(null);
            } else if (request.getReminderDaysBefore() != null) {
                entity.updateReminders(request.getReminderDaysBefore());
            }
        } else if (request.getReminderDaysBefore() != null) {
            entity.updateReminders(request.getReminderDaysBefore());
        }
    }

    // ==================== 돌봄 일정 삭제 ====================
    
    /**
     * 돌봄 일정 삭제 (soft delete)
     */
    public Long deleteCareSchedule(Long calNo, Long userNo) {
        Schedule entity = findScheduleById(calNo);



        if (!entity.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }

        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "이미 삭제된 일정입니다.");
        }

        deleteSchedule(calNo);
        return calNo;
    }

    // ==================== 알림 관리 ====================
    
    /**
     * 알림 활성화/비활성화 토글
     */
    public Boolean toggleAlarm(Long calNo, Long userNo) {
        Schedule entity = findScheduleById(calNo);

        if (!entity.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }

        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "삭제된 일정입니다.");
        }

        if (entity.getMainType() != ScheduleMainType.CARE) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "돌봄 일정이 아닙니다.");
        }

        // 현재 알림 상태 확인
        boolean currentAlarmEnabled = entity.getReminderDaysBefore() != null;
        boolean newAlarmState = !currentAlarmEnabled;
        
        if (newAlarmState) {
            // 알림 활성화: 마지막 알림 시기가 있으면 복원, 없으면 기본값(1일전) 설정
            Integer lastReminderDays = entity.getLastReminderDaysBefore();
            if (lastReminderDays != null) {
                entity.updateReminders(lastReminderDays);
                log.info("알림 활성화: 마지막 설정값({}일전) 복원 - scheduleNo={}", lastReminderDays, calNo);
            } else {
                entity.updateReminders(1); // 기본값: 1일 전 알림
                log.info("알림 활성화: 기본값(1일전)으로 설정 - scheduleNo={}", calNo);
            }
        } else {
            // 알림 비활성화: reminderDaysBefore를 null로 설정 (lastReminderDaysBefore는 유지)
            entity.updateReminders(null);
            log.info("알림 비활성화 - scheduleNo={}, 마지막 알림시기 유지: {}일전", calNo, entity.getLastReminderDaysBefore());
        }
        
        scheduleRepository.save(entity);
        return newAlarmState;
    }



    // ==================== 메타 정보 조회 ====================
    
    /**
     * 돌봄 및 접종 관련 메타 정보 조회 (드롭다운용)
     */
    public Map<String, List<String>> getCareMeta() {
        List<String> subTypes = Arrays.stream(ScheduleSubType.values())
                .filter(ScheduleSubType::isCareType)
                .map(Enum::name)
                .toList();
        List<String> frequencies = Arrays.stream(CareFrequency.values())
                .map(Enum::name)
                .toList();
        Map<String, List<String>> data = new HashMap<>();
        data.put("subTypes", subTypes);
        data.put("frequencies", frequencies);
        return data;
    }

    // ==================== 날짜 검증 메서드 ====================
    
    /**
     * 돌봄 일정 날짜 범위 검증 및 종료일 자동 계산
     */
    private LocalDate validateAndCalculateEndDate(LocalDate startDate, LocalDate endDate, CareFrequency frequency) {
        // 시작일이 과거인지 확인
        if (startDate.isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.MEDICAL_DATE_PAST_ERROR, 
                "과거 날짜로 일정을 생성할 수 없습니다.");
        }

        // 빈도별 종료일 검증 및 자동 계산
        switch (frequency) {
            case DAILY:
                // 매일: 종료날짜가 있으면 그대로 사용, 없으면 시작일과 동일
                if (endDate != null) {
                    if (endDate.isBefore(startDate)) {
                        throw new BusinessException(ErrorCode.MEDICAL_DATE_RANGE_ERROR,
                                "종료일은 시작일보다 이전일 수 없습니다.");
                    }
                    return endDate;  // 사용자가 선택한 종료날짜 사용
                }
                return startDate;  // 종료날짜 없으면 시작일과 동일

            case WEEKLY:
                // 매주: 최소 7일 이후의 종료날짜만 허용
                if (endDate != null) {
                    LocalDate minEndDate = startDate.plusDays(7);
                    if (endDate.isBefore(minEndDate)) {
                        throw new BusinessException(ErrorCode.MEDICAL_DATE_RANGE_ERROR,
                                "매주 일정의 종료일은 시작일로부터 최소 7일 이후여야 합니다. 최소 종료일: " + minEndDate);
                    }
                    return endDate;
                }
                // 종료일이 없으면 1주일 후로 설정
                return startDate.plusWeeks(1);

            case MONTHLY:
                // 매월: 최소 30일 이후의 종료날짜만 허용
                if (endDate != null) {
                    LocalDate minEndDate = startDate.plusDays(30);
                    if (endDate.isBefore(minEndDate)) {
                        throw new BusinessException(ErrorCode.MEDICAL_DATE_RANGE_ERROR,
                                "매월 일정의 종료일은 시작일로부터 최소 30일 이후여야 합니다. 최소 종료일: " + minEndDate);
                    }
                    return endDate;
                }
                // 종료일이 없으면 1개월 후로 설정
                return startDate.plusMonths(1);

            case YEARLY_ONCE:
                // 연 1회: 최소 365일 이후의 종료날짜만 허용
                if (endDate != null) {
                    LocalDate minEndDate = startDate.plusDays(365);
                    if (endDate.isBefore(minEndDate)) {
                        throw new BusinessException(ErrorCode.MEDICAL_DATE_RANGE_ERROR,
                                "연 1회 일정의 종료일은 시작일로부터 최소 365일 이후여야 합니다. 최소 종료일: " + minEndDate);
                    }
                    return endDate;
                }
                // 종료일이 없으면 1년 후로 설정
                return startDate.plusYears(1);

            case HALF_YEARLY_ONCE:
                // 반년 1회: 최소 6개월 이후의 종료날짜만 허용
                if (endDate != null) {
                    LocalDate minEndDate = startDate.plusMonths(6);
                    if (endDate.isBefore(minEndDate)) {
                        throw new BusinessException(ErrorCode.MEDICAL_DATE_RANGE_ERROR,
                                "반년 1회 일정의 종료일은 시작일로부터 최소 6개월 이후여야 합니다. 최소 종료일: " + minEndDate);
                    }
                    return endDate;
                }
                // 종료일이 없으면 6개월 후로 설정
                return startDate.plusMonths(6);

            default:
                // 기본값: 종료일이 시작일보다 이전인지 확인
                if (endDate != null && endDate.isBefore(startDate)) {
                    throw new BusinessException(ErrorCode.MEDICAL_DATE_RANGE_ERROR,
                            "종료일은 시작일보다 이전일 수 없습니다.");
                }
                return endDate != null ? endDate : startDate;
        }
    }
    
    /**
     * 월만 입력된 경우 해당 월의 마지막 날로 계산 (매주, 매월용)
     */
    private LocalDate calculateMonthlyEndDate(LocalDate startDate, LocalDate endDate, String frequencyType) {
        // 종료일이 해당 월의 1일인 경우 (예: 2024-02-01)
        if (endDate.getDayOfMonth() == 1) {
            // 해당 월의 마지막 날로 계산
            LocalDate lastDayOfMonth = endDate.withDayOfMonth(endDate.lengthOfMonth());
            
            // 시작일이 해당 월보다 이후인지 확인
            if (startDate.isAfter(lastDayOfMonth)) {
                throw new BusinessException(ErrorCode.MEDICAL_DATE_RANGE_ERROR, 
                    frequencyType + " 일정의 시작일이 종료월보다 이후일 수 없습니다.");
            }
            
            return lastDayOfMonth;
        }
        
        // 정확한 날짜가 입력된 경우
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.MEDICAL_DATE_RANGE_ERROR, 
                "종료일은 시작일보다 이전일 수 없습니다.");
        }
        
        return endDate;
    }
    
    /**
     * 년도만 입력된 경우 해당 년도의 시작일과 동일한 날짜로 계산 (연 1회용)
     */
    private LocalDate calculateYearlyEndDate(LocalDate startDate, LocalDate endDate) {
        // 종료일이 해당 년도의 1월 1일인 경우 (예: 2025-01-01)
        if (endDate.getMonthValue() == 1 && endDate.getDayOfMonth() == 1) {
            // 시작일과 동일한 월일로 다음 년도에 설정
            LocalDate nextYearSameDate = startDate.withYear(endDate.getYear());
            
            // 시작일이 종료년도보다 이후인지 확인
            if (startDate.getYear() > endDate.getYear()) {
                throw new BusinessException(ErrorCode.MEDICAL_DATE_RANGE_ERROR, 
                    "연 1회 일정의 시작일이 종료년도보다 이후일 수 없습니다.");
            }
            
            return nextYearSameDate;
        }
        
        // 정확한 날짜가 입력된 경우
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.MEDICAL_DATE_RANGE_ERROR, 
                "종료일은 시작일보다 이전일 수 없습니다.");
        }
        
        return endDate;
    }

    // ==================== 이벤트 발행 ====================
    
    /**
     * 스케줄 생성 이벤트 발행
     */
    private void publishScheduleCreatedEvent(Schedule schedule) {
        try {
            EventMessage event = new EventMessage();
            event.setEventId(UUID.randomUUID().toString());
            event.setType("health.schedule");
            event.setOccurredAt(Instant.now());
            event.setActor(new EventMessage.Actor(schedule.getUserNo(), "User"));
            event.setTarget(new EventMessage.Target(
                schedule.getUserNo().toString(),
                schedule.getScheduleNo(),
                "SCHEDULE"));
            
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("scheduleId", schedule.getScheduleNo());
            attributes.put("title", schedule.getTitle());
            attributes.put("mainType", schedule.getMainType().name());
            attributes.put("subType", schedule.getSubType().name());
            attributes.put("startDate", schedule.getStartDate());
            attributes.put("reminderDaysBefore", schedule.getReminderDaysBefore());
            
            // durationDays와 times 데이터 추가
            Integer durationDays = null;
            List<String> timesList = null;
            
            // MEDICATION 타입인 경우 ScheduleMedDetail에서 durationDays 가져오기
            if (schedule.getMainType() == ScheduleMainType.MEDICATION) {
                var detailOpt = medicationDetailRepository.findById(schedule.getScheduleNo());
                if (detailOpt.isPresent()) {
                    durationDays = detailOpt.get().getDurationDays();
                }
            } else {
                // CARE/VACCINATION 타입인 경우 시작일과 종료일의 차이로 계산
                if (schedule.getStartDate() != null && schedule.getEndDate() != null) {
                    durationDays = (int) java.time.temporal.ChronoUnit.DAYS.between(
                        schedule.getStartDate().toLocalDate(), 
                        schedule.getEndDate().toLocalDate()
                    ) + 1; // 시작일 포함
                }
            }
            
            // times를 List<String> 형태로 변환
            if (schedule.getTimes() != null && !schedule.getTimes().isEmpty()) {
                timesList = schedule.getTimesAsList().stream()
                    .map(LocalTime::toString)
                    .toList();
            }
            
            attributes.put("durationDays", durationDays);
            attributes.put("times", timesList);
            event.setAttributes(attributes);
            event.setSchemaVersion(1);
            
            // notif.events로 메시지 발행
            rabbitTemplate.convertAndSend("notif.events", "health.schedule", event);
            
            log.info("스케줄 생성 이벤트 발행 완료: scheduleNo={}, title={}, durationDays={}, times={}", 
                    schedule.getScheduleNo(), schedule.getTitle(), durationDays, timesList);
        } catch (Exception e) {
            log.error("스케줄 생성 이벤트 발행 실패: scheduleNo={}, error={}", 
                    schedule.getScheduleNo(), e.getMessage(), e);
        }
    }
}


