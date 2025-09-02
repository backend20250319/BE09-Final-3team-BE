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


        ScheduleSubType subType = request.getSubType();
        
        ScheduleMainType mainType;
        if (subType.isVaccinationType()) {
            mainType = ScheduleMainType.VACCINATION;
        } else {
            mainType = ScheduleMainType.CARE;
        }

        // 공통 DTO로 변환
        ScheduleRequestDTO commonRequest = ScheduleRequestDTO.builder()
                .petNo(request.getPetNo())
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .subType(request.getSubType())
                .times(request.getTimes())
                .frequency(RecurrenceType.DAILY)
                .recurrenceInterval(1)
                .recurrenceEndDate(request.getEndDate())
                .reminderDaysBefore(request.getReminderDaysBefore())
                .frequencyText("매일")
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
                .filter(c -> c.getMainType() == ScheduleMainType.CARE || c.getMainType() == ScheduleMainType.VACCINATION)
                .filter(c -> c.getPetNo().equals(petNo)); // 특정 펫의 일정만 필터링
        
        if (subType != null && !subType.isBlank()) {
            try {
                ScheduleSubType targetSubType = ScheduleSubType.valueOf(subType.toUpperCase());
                
                if (targetSubType.isVaccinationType()) {
                    // 접종 관련 서브타입이면 VACCINATION 메인타입만
                    stream = stream.filter(c -> c.getMainType() == ScheduleMainType.VACCINATION);
                } else {
                    // 일반 돌봄 서브타입이면 CARE 메인타입만
                    stream = stream.filter(c -> c.getMainType() == ScheduleMainType.CARE);
                }
                
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
        
        if (entity.getMainType() != ScheduleMainType.CARE && entity.getMainType() != ScheduleMainType.VACCINATION) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "돌봄 또는 접종 일정이 아닙니다.");
        }
        
        // 메인타입 변경 방지
        if (request.getSubType() != null) {
            ScheduleSubType newSubType = request.getSubType();
            if (entity.getMainType() == ScheduleMainType.CARE && newSubType.isVaccinationType()) {
                throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "돌봄 일정을 접종 일정으로 변경할 수 없습니다.");
            }
            if (entity.getMainType() == ScheduleMainType.VACCINATION && !newSubType.isVaccinationType()) {
                throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "접종 일정을 돌봄 일정으로 변경할 수 없습니다.");
            }
        }

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

        if (entity.getMainType() != ScheduleMainType.CARE && entity.getMainType() != ScheduleMainType.VACCINATION) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "돌봄 또는 접종 일정이 아닙니다.");
        }

        // 현재 알림 상태를 반대로 토글
        boolean currentAlarmEnabled = entity.getReminderDaysBefore() != null;
        if (currentAlarmEnabled) {
            // 알림 비활성화
            super.toggleAlarm(calNo, false);
        } else {
            // 알림 활성화 (기본값: 당일 알림)
            super.toggleAlarm(calNo, true);
        }

        return !currentAlarmEnabled; // 새로운 알림 상태 반환
    }



    // ==================== 메타 정보 조회 ====================
    
    /**
     * 돌봄 및 접종 관련 메타 정보 조회 (드롭다운용)
     */
    public java.util.Map<String, java.util.List<String>> getCareMeta() {
        java.util.List<String> subTypes = java.util.Arrays.stream(ScheduleSubType.values())
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


