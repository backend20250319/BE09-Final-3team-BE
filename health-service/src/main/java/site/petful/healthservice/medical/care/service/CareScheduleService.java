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
        // 날짜 검증
        validateDateRange(request.getStartDate(), request.getEndDate());

        ScheduleSubType subType = request.getSubType();
        
        ScheduleMainType mainType = ScheduleMainType.CARE;

        // careFrequency 처리
        CareFrequency careFreq = request.getCareFrequency() != null ? request.getCareFrequency() : CareFrequency.DAILY;
        RecurrenceType recurrenceType = careFreq.getRecurrenceType();
        Integer interval = careFreq.getInterval();
        String frequencyText = careFreq.getLabel();

        // 공통 DTO로 변환
        ScheduleRequestDTO commonRequest = ScheduleRequestDTO.builder()
                .petNo(request.getPetNo())
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .subType(request.getSubType())
                .times(request.getTimes())
                .frequency(recurrenceType)
                .recurrenceInterval(interval)
                .recurrenceEndDate(request.getEndDate())
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

        // 날짜 검증
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : entity.getStartDate().toLocalDate();
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : entity.getEndDate().toLocalDate();
        validateDateRange(startDate, endDate);

        // 일정 업데이트
        updateCareScheduleFields(entity, request);
        
        // 엔티티 저장
        scheduleRepository.save(entity);
        return entity.getScheduleNo();
    }

    private void updateCareScheduleFields(Schedule entity, CareUpdateRequestDTO request) {
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
     * 날짜 범위 검증
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        // 시작일이 과거인지 확인
        if (startDate.isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.MEDICAL_DATE_PAST_ERROR, 
                "과거 날짜로 일정을 생성할 수 없습니다.");
        }
        
        // 종료일이 시작일보다 이전인지 확인
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.MEDICAL_DATE_RANGE_ERROR, 
                "종료일은 시작일보다 이전일 수 없습니다.");
        }
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


