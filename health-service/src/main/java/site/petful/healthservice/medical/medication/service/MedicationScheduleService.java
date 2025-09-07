package site.petful.healthservice.medical.medication.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.petful.healthservice.medical.medication.entity.ScheduleMedDetail;
import site.petful.healthservice.medical.schedule.entity.Schedule;
import site.petful.healthservice.medical.schedule.enums.ScheduleMainType;
import site.petful.healthservice.medical.schedule.enums.ScheduleSubType;
import site.petful.healthservice.medical.schedule.enums.RecurrenceType;
import site.petful.healthservice.medical.medication.enums.MedicationFrequency;
import site.petful.healthservice.medical.medication.enums.FrequencyConversion;
import site.petful.healthservice.medical.medication.dto.PrescriptionParsedDTO;
import site.petful.healthservice.medical.medication.dto.MedicationRequestDTO;
import site.petful.healthservice.medical.medication.dto.MedicationResponseDTO;
import site.petful.healthservice.medical.medication.dto.MedicationDetailDTO;
import site.petful.healthservice.medical.medication.dto.MedicationUpdateRequestDTO;
import site.petful.healthservice.medical.medication.dto.MedicationUpdateDiffDTO;
import site.petful.healthservice.medical.medication.repository.ScheduleMedicationDetailRepository;
import site.petful.healthservice.medical.schedule.repository.ScheduleRepository;
import site.petful.healthservice.medical.schedule.service.AbstractScheduleService;
import site.petful.healthservice.medical.schedule.dto.ScheduleRequestDTO;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.common.response.ErrorCode;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.client.PetServiceClient;
import site.petful.healthservice.common.dto.PetResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class MedicationScheduleService extends AbstractScheduleService {

    private final ScheduleMedicationDetailRepository medicationDetailRepository;
    private final MedicationEventService eventService;
    private final MedicationFrequencyService frequencyService;
    private final MedicationOCRService ocrService;

    public MedicationScheduleService(ScheduleRepository scheduleRepository, 
                                   ScheduleMedicationDetailRepository medicationDetailRepository,
                                   MedicationEventService eventService,
                                   MedicationFrequencyService frequencyService,
                                   MedicationOCRService ocrService) {
        super(scheduleRepository);
        this.medicationDetailRepository = medicationDetailRepository;
        this.eventService = eventService;
        this.frequencyService = frequencyService;
        this.ocrService = ocrService;
    }


    /**
     * 복용약/영양제 일정 생성 (캘린더 기반)
     */
    public Long createMedication(Long userNo, @Valid MedicationRequestDTO request) {
        // 시작날짜 검증
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.MEDICAL_START_DATE_PAST_ERROR, 
                "시작날짜는 당일보다 이전일 수 없습니다.");
        }

        // 종료날짜 검증 (시작일 + 기간 - 1일)
        LocalDate endDate = request.getStartDate().plusDays(request.getDurationDays() - 1);
        if (endDate.isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.MEDICAL_END_DATE_PAST_ERROR, 
                "종료날짜는 당일보다 이전일 수 없습니다.");
        }

        // 디버깅 로그 추가
        log.info("=== 투약 일정 생성 디버깅 ===");
        log.info("요청된 medicationFrequency: {}", request.getMedicationFrequency());
        log.info("RecurrenceType: {}", request.getMedicationFrequency().getRecurrenceType());
        log.info("Interval: {}", request.getMedicationFrequency().getInterval());
        log.info("Label: {}", request.getMedicationFrequency().getLabel());

    
        ScheduleRequestDTO commonRequest = ScheduleRequestDTO.builder()
                .petNo(request.getPetNo())
                .title(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getStartDate().plusDays(request.getDurationDays() - 1))
                .subType(request.getSubType() != null ? request.getSubType() : ScheduleSubType.PILL)  // 요청에서 subType 사용, 없으면 기본값 PILL
                .times(request.getTimes())
                .frequency(request.getMedicationFrequency().getRecurrenceType()) 
                .recurrenceInterval(request.getMedicationFrequency().getInterval()) 
                .recurrenceEndDate(request.getStartDate().plusDays(request.getDurationDays() - 1))
                .reminderDaysBefore(request.getReminderDaysBefore())
                .frequencyText(request.getMedicationFrequency().getLabel())
                .build();

        // 상속된 공통 서비스 사용
        Schedule entity = createScheduleEntity(userNo, commonRequest, ScheduleMainType.MEDICATION);
        Long scheduleNo = saveSchedule(entity);

        
        // ScheduleMedDetail 저장 후 스케줄 생성 이벤트 발행
        eventService.publishScheduleCreatedEvent(entity);

        return scheduleNo;
    }



    /**
     * 파싱된 처방전 정보를 기반으로 투약 일정을 생성/저장합니다.
     */
    public List<Schedule> registerMedicationSchedules(PrescriptionParsedDTO parsed, Long userNo, Long petNo, LocalDate baseDate) {
        return registerMedicationSchedules(parsed, userNo, petNo, baseDate, ScheduleSubType.PILL);
    }

    /**
     * 서브타입을 지정하여 일정을 생성/저장합니다.
     */
    public List<Schedule> registerMedicationSchedules(PrescriptionParsedDTO parsed, Long userNo, Long petNo, LocalDate baseDate, ScheduleSubType subType) {

        List<Schedule> created = new ArrayList<>();
        if (parsed == null || parsed.getMedications() == null || parsed.getMedications().isEmpty()) {
            return created;
        }

        LocalDate startDay = baseDate != null ? baseDate : LocalDate.now();

        for (PrescriptionParsedDTO.MedicationInfo med : parsed.getMedications()) {
            String drugName = med.getDrugName();
            String dosage = med.getDosage();
            String administration = med.getAdministration();
            String frequencyText = med.getFrequency();

            int durationDays = ocrService.extractDays(med.getPrescriptionDays());
            if (durationDays <= 0) durationDays = 1;

            MedicationFrequencyService.FrequencyInfo freqInfo = frequencyService.parseFrequency(frequencyText);
            int timesPerDay = freqInfo.getTimesPerDay() > 0 ? freqInfo.getTimesPerDay() : 1;

            List<LocalTime> slots;
            if (med.getTimes() != null && !med.getTimes().isEmpty()) {
                slots = med.getTimes();
            } else {
                slots = defaultSlots(timesPerDay);
            }

            LocalDateTime startDateTime = LocalDateTime.of(startDay, slots.get(0));
            LocalDate endDay = startDay.plusDays(Math.max(0, durationDays - 1));
            LocalDateTime endDateTime = LocalDateTime.of(endDay, slots.get(slots.size() - 1));

                            // 기본값으로 DAILY 설정
        RecurrenceType recurrenceType = RecurrenceType.DAILY;
        int interval = 1;
        
        // 공통 DTO로 변환
        ScheduleRequestDTO commonRequest = ScheduleRequestDTO.builder()
                .petNo(petNo) // 파라미터로 받은 petNo 사용
                .title(ocrService.buildTitle(drugName, dosage))
                .startDate(startDay)
                .endDate(endDay)
                .subType(subType != null ? subType : ScheduleSubType.PILL)
                .times(slots)
                .frequency(recurrenceType)
                .recurrenceInterval(interval)
                .recurrenceEndDate(endDay)
                .reminderDaysBefore(0)  // 기본값: 당일 알림
                .frequencyText(frequencyText)
                .build();

                    Schedule entity = createScheduleEntity(userNo, commonRequest, ScheduleMainType.MEDICATION);
                    Long savedId = saveSchedule(entity);
                    
                    // 저장된 ID 확인
                    if (savedId == null) {
                        throw new BusinessException(ErrorCode.SCHEDULE_SAVE_FAILED, "스케줄 저장 후 ID가 null입니다.");
                    }

                    // 상세 정보 저장 - 더 안전한 방식으로 변경
        try {
            // Schedule 엔티티를 다시 조회하여 최신 상태 확인
            Schedule savedSchedule = scheduleRepository.findById(savedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND, "저장된 스케줄을 찾을 수 없습니다: " + savedId));
            
            // 새로운 ScheduleMedDetail 엔티티 생성 (ID 없음)
            ScheduleMedDetail detail = new ScheduleMedDetail();
            detail.setScheduleNo(savedId);
            detail.setMedicationName(drugName);
            detail.setDosage(dosage);
            detail.setDurationDays(durationDays);
            detail.setIsPrescription(true);     // OCR 처방전은 처방전으로 등록
            detail.setOcrRawData(parsed.getOriginalText());
            
            // saveAndFlush로 즉시 DB에 반영
            ScheduleMedDetail savedDetail = medicationDetailRepository.saveAndFlush(detail);
            if (savedDetail.getScheduleNo() == null) {
                throw new BusinessException(ErrorCode.MEDICATION_DETAIL_SAVE_FAILED, "상세 정보 저장 후 schedule_no가 null입니다.");
            }
            
            // 디버깅용 로그
            log.info("=== ScheduleMedDetail 저장 성공 ===");
            log.info("schedule_no: {}", savedDetail.getScheduleNo());
            log.info("medication_name: {}", savedDetail.getMedicationName());
            
            // ScheduleMedDetail 저장 후 스케줄 생성 이벤트 발행
            eventService.publishScheduleCreatedEvent(savedSchedule);
            
        } catch (BusinessException e) {
            throw e; // BusinessException은 그대로 던지기
        } catch (Exception e) {
            log.error("투약 상세 정보 저장 중 예외 발생", e);
            throw new BusinessException(ErrorCode.MEDICATION_DETAIL_SAVE_FAILED, "투약 상세 정보 저장 실패: " + e.getMessage());
        }

            created.add(entity);
        }

        return created;
    }
    

    
    /**
     * 투약 일정 목록 조회
     */
    public List<MedicationResponseDTO> listMedications(Long userNo, Long petNo, String from, String to, String subType, String type, Boolean isPrescription) {


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
            throw new BusinessException(ErrorCode.MEDICAL_DATE_FORMAT_ERROR, "건강관리 일정의 날짜 형식이 올바르지 않습니다.");
        }
        if (start.isAfter(end)) {
            throw new BusinessException(ErrorCode.MEDICAL_DATE_RANGE_ERROR, "건강관리 일정의 날짜 범위가 올바르지 않습니다.");
        }

        // N+1 문제 해결: 한 번의 쿼리로 모든 데이터 조회
        List<Schedule> items = scheduleRepository.findSchedulesWithMedicationDetails(userNo, start, end);

        Stream<Schedule> stream = items.stream()
                .filter(c -> c.getMainType() == ScheduleMainType.MEDICATION)
                .filter(c -> c.getPetNo().equals(petNo)); // 특정 펫의 일정만 필터링
        
        // 기존 subType 필터링 (하위 호환성 유지)
        if (subType != null && !subType.isBlank()) {
            stream = stream.filter(c -> c.getSubType().name().equalsIgnoreCase(subType));
        }
        
        // 새로운 type 필터링 (복용약, 영양제)
        if (type != null && !type.isBlank()) {
            stream = stream.filter(c -> c.getSubType().name().equalsIgnoreCase(type));
        }

        // N+1 문제 해결: 모든 상세 정보를 한 번에 조회
        List<Schedule> filteredSchedules = stream.collect(Collectors.toList());
        List<Long> scheduleNos = filteredSchedules.stream()
                .map(Schedule::getScheduleNo)
                .collect(Collectors.toList());
        Map<Long, ScheduleMedDetail> detailMap = medicationDetailRepository.findAllById(scheduleNos)
                .stream()
                .collect(Collectors.toMap(ScheduleMedDetail::getScheduleNo, detail -> detail));
        
        return filteredSchedules.stream()
                .map(c -> {
                    ScheduleMedDetail detail = detailMap.get(c.getScheduleNo());
                    String medName = detail != null ? detail.getMedicationName() : null;
                    String dosage = detail != null ? detail.getDosage() : null;
                    Integer durationDays = detail != null ? detail.getDurationDays() : null;
                    Boolean isPrescriptionMedication = detail != null ? detail.getIsPrescription() : false;

                    // 디버깅 로그 추가
                    log.info("=== 투약 일정 조회 디버깅 ===");
                    log.info("scheduleNo: {}", c.getScheduleNo());
                    log.info("저장된 frequency: {}", c.getFrequency());
                    log.info("저장된 recurrenceType: {}", c.getRecurrenceType());
                    log.info("저장된 recurrenceInterval: {}", c.getRecurrenceInterval());

                    MedicationFrequencyService.FrequencyInfo freqInfo = frequencyService.parseFrequency(c.getFrequency());
                    List<LocalTime> slots = c.getTimesAsList() != null && !c.getTimesAsList().isEmpty() 
                        ? c.getTimesAsList() 
                        : defaultSlots(freqInfo.getTimesPerDay());
                    
                    String normalizedFrequency = frequencyService.normalizeFrequency(c.getFrequency());
                    log.info("정규화된 frequency: {}", normalizedFrequency);
                    
                    return MedicationResponseDTO.builder()
                            .scheduleNo(c.getScheduleNo())
                            .title(c.getTitle())
                            .startDate(c.getStartDate())
                            .endDate(c.getEndDate())
                            .mainType(c.getMainType().name())
                            .subType(c.getSubType().getLabel())
                            .medicationName(medName)
                            .dosage(dosage)
                            .frequency(normalizedFrequency)
                            .durationDays(durationDays)
                            .time(c.getStartDate() != null ? c.getStartDate().toLocalTime() : null)
                            .times(slots)
                            .reminderDaysBefore(c.getReminderDaysBefore())
                            .lastReminderDaysBefore(c.getLastReminderDaysBefore())
                            .alarmEnabled(c.getReminderDaysBefore() != null)  // 알림 설정 여부
                            .isPrescription(isPrescriptionMedication)
                            .build();
                })
                .filter(dto -> {
                    // isPrescription 필터링
                    if (isPrescription != null) {
                        return dto.getIsPrescription().equals(isPrescription);
                    }
                    return true;
                })
                .toList();
    }

    /**
     * 투약 일정 상세 조회
     */
    public MedicationDetailDTO getMedicationDetail(Long calNo, Long userNo) {
        Schedule c = findScheduleById(calNo);



        if (!c.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }
        if (Boolean.TRUE.equals(c.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "삭제된 일정입니다.");
        }
        if (c.getMainType() != ScheduleMainType.MEDICATION) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "투약 일정이 아닙니다.");
        }

        Optional<ScheduleMedDetail> detailOpt = medicationDetailRepository.findById(calNo);
        String medName = detailOpt.map(ScheduleMedDetail::getMedicationName).orElse(null);
        String dosage = detailOpt.map(ScheduleMedDetail::getDosage).orElse(null);
        Integer duration = detailOpt.map(ScheduleMedDetail::getDurationDays).orElse(null);
        Boolean isPrescription = detailOpt.map(ScheduleMedDetail::getIsPrescription).orElse(false);

        // 기본 시간 슬롯 사용 (상세 조회 시에는 기본 시간으로 표시)
        MedicationFrequencyService.FrequencyInfo freqInfo = frequencyService.parseFrequency(c.getFrequency());
        List<LocalTime> slots = defaultSlots(freqInfo.getTimesPerDay());

        return MedicationDetailDTO.builder()
                .scheduleNo(c.getScheduleNo())
                .title(c.getTitle())
                .mainType(c.getMainType().name())
                .subType(c.getSubType().name())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .time(c.getStartDate() != null ? c.getStartDate().toLocalTime() : null)
                .times(slots)
                .frequency(c.getFrequency())
                        .alarmEnabled(c.getReminderDaysBefore() != null)
        .reminderDaysBefore(c.getReminderDaysBefore())
                .lastReminderDaysBefore(c.getLastReminderDaysBefore())
                .medicationName(medName)
                .dosage(dosage)
                .durationDays(duration)
                .isPrescription(isPrescription)
                .build();
    }
    
    /**
     * 투약 일정 수정 (부분 업데이트)
     */
    
    public MedicationUpdateDiffDTO updateMedication(Long calNo, MedicationUpdateRequestDTO request, Long userNo) {
        return updateMedication(calNo, request, userNo, true);
    }
    
    public MedicationUpdateDiffDTO updateMedication(Long calNo, MedicationUpdateRequestDTO request, Long userNo, boolean isEditMode) {
        // 조회 및 소유자 검증
        Schedule entity = findScheduleById(calNo);
        

        
        if (entity.getMainType() != ScheduleMainType.MEDICATION) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "투약 일정이 아닙니다.");
        }
        
        if (!entity.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }
        
        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "삭제된 일정입니다.");
        }

        // 투약 일정 수정 시 조건부 시작날짜 수정 허용
        if (request.getStartDate() != null) {
            LocalDate today = LocalDate.now();
            LocalDate originalStartDate = entity.getStartDate().toLocalDate();
            
            if (originalStartDate.isBefore(today)) {
                // 과거에 시작된 일정은 시작날짜 수정 불가
                if (!request.getStartDate().equals(originalStartDate)) {
                    throw new BusinessException(ErrorCode.MEDICAL_START_DATE_PAST_ERROR, 
                        "과거에 시작된 투약 일정은 시작날짜를 변경할 수 없습니다.");
                }
            } else {
                // 미래에 시작될 일정은 시작날짜 수정 가능 (단, 오늘 이전으로는 불가)
                if (request.getStartDate().isBefore(today)) {
                    throw new BusinessException(ErrorCode.MEDICAL_START_DATE_PAST_ERROR, 
                        "시작날짜는 당일보다 이전일 수 없습니다.");
                }
            }
        }

        // 종료날짜 검증 (수정 시 시작날짜나 기간이 변경되는 경우)
        if (request.getStartDate() != null || request.getDurationDays() != null) {
            LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : entity.getStartDate().toLocalDate();
            Integer durationDays = request.getDurationDays() != null ? request.getDurationDays() : 
                (entity.getStartDate() != null && entity.getEndDate() != null ? 
                    (int) java.time.temporal.ChronoUnit.DAYS.between(entity.getStartDate().toLocalDate(), entity.getEndDate().toLocalDate()) + 1 : 1);
            
            LocalDate endDate = startDate.plusDays(durationDays - 1);
            if (endDate.isBefore(LocalDate.now())) {
                throw new BusinessException(ErrorCode.MEDICAL_END_DATE_PAST_ERROR, 
                    "종료날짜는 당일보다 이전일 수 없습니다.");
            }
        }

        // 변경 전 스냅샷 수집
        MedicationUpdateDiffDTO.Snapshot before = createSnapshot(entity);
        
        // 일정 업데이트
        updateMedicationSchedule(entity, request);
        
        // 변경 후 스냅샷 수집
        MedicationUpdateDiffDTO.Snapshot after = createSnapshot(entity);
        
        return MedicationUpdateDiffDTO.builder()
                .before(before)
                .after(after)
                .build();
    }

    private MedicationUpdateDiffDTO.Snapshot createSnapshot(Schedule entity) {
        Optional<ScheduleMedDetail> detailOpt = medicationDetailRepository.findById(entity.getScheduleNo());
        ScheduleMedDetail detail = detailOpt.orElse(null);
        
        Integer reminder = entity.getReminderDaysBefore();
        
        return MedicationUpdateDiffDTO.Snapshot.builder()
                .title(entity.getTitle())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .medicationName(detail != null ? detail.getMedicationName() : null)
                .dosage(detail != null ? detail.getDosage() : null)
                .frequency(entity.getFrequency())
                .durationDays(detail != null ? detail.getDurationDays() : null)
                .subType(entity.getSubType().name())
                .reminderDaysBefore(reminder)
                .build();
    }

    private void updateMedicationSchedule(Schedule entity, MedicationUpdateRequestDTO request) {
        // 제목 업데이트
        if (request.getMedicationName() != null || request.getDosage() != null) {
            String medName = request.getMedicationName() != null ? request.getMedicationName() : 
                (entity.getTitle() != null ? entity.getTitle().split(" ")[0] : "");
            String dosage = request.getDosage() != null ? request.getDosage() : "";
            entity.updateSchedule(medName + " " + dosage, entity.getStartDate(), entity.getEndDate(), entity.getAlarmTime());
        }

        // 기본값 설정 - ScheduleMedDetail에서 durationDays 조회
        LocalDate base = request.getStartDate() != null ? request.getStartDate() : entity.getStartDate().toLocalDate();
        Integer duration = request.getDurationDays();
        
        // durationDays가 요청에 없으면 ScheduleMedDetail에서 조회
        if (duration == null) {
            Optional<ScheduleMedDetail> detailOpt = medicationDetailRepository.findById(entity.getScheduleNo());
            duration = detailOpt.map(ScheduleMedDetail::getDurationDays).orElse(1);
        }
        // administration 필드는 더 이상 사용하지 않음
        String freq = request.getFrequency() != null ? request.getFrequency() : entity.getFrequency();

        // 빈도/시간 재계산
        MedicationFrequencyService.FrequencyInfo freqInfo = frequencyService.parseFrequency(freq);
        List<LocalTime> slots;
        if (request.getTimes() != null && !request.getTimes().isEmpty()) {
            slots = request.getTimes();  // 사용자가 입력한 시간들 직접 사용
        } else {
            slots = defaultSlots(freqInfo.getTimesPerDay());  // 기본 시간 사용
        }

        LocalDateTime startDt = LocalDateTime.of(base, slots.get(0));
        LocalDate endDay = base.plusDays(Math.max(0, duration - 1));
        LocalDateTime endDt = LocalDateTime.of(endDay, slots.get(slots.size() - 1));

        // 복용기간이 변경된 경우 종료날짜 재계산하여 업데이트
        entity.updateSchedule(entity.getTitle(), startDt, endDt, startDt);
        entity.updateFrequency(freq);
        entity.updateRecurrence(freqInfo.getRecurrenceType(), freqInfo.getInterval(), endDt);

        // 서브타입 변경
        if (request.getSubType() != null) {
            entity.updateSubType(request.getSubType());
        }

        // 알림 변경
        if (request.getReminderDaysBefore() != null) {
            entity.updateReminders(request.getReminderDaysBefore());
        }

        // 엔티티 저장
        scheduleRepository.save(entity);

        // 상세 정보 업데이트
        updateMedicationDetail(entity.getScheduleNo(), request);
    }

    private void updateMedicationDetail(Long calNo, MedicationUpdateRequestDTO request) {
        Optional<ScheduleMedDetail> detailOpt = medicationDetailRepository.findById(calNo);
        if (detailOpt.isPresent()) {
            ScheduleMedDetail detail = detailOpt.get();
            if (request.getMedicationName() != null) detail.setMedicationName(request.getMedicationName());
            if (request.getDosage() != null) detail.setDosage(request.getDosage());
            if (request.getDurationDays() != null) detail.setDurationDays(request.getDurationDays());
            medicationDetailRepository.save(detail);
        }
    }
    
    /**
     * 투약 일정 알림 on/off
     */
    public Boolean toggleAlarm(Long calNo, Long userNo) {
        Schedule entity = findScheduleById(calNo);

        if (entity.getMainType() != ScheduleMainType.MEDICATION) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "투약 일정이 아닙니다.");
        }

        if (!entity.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }
        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "삭제된 일정입니다.");
        }

        // 처방전인 경우 알림 시기 변경 제한
        Optional<ScheduleMedDetail> detailOpt = medicationDetailRepository.findById(calNo);
        Boolean isPrescription = detailOpt.map(ScheduleMedDetail::getIsPrescription).orElse(false);
        if (Boolean.TRUE.equals(isPrescription)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "처방전으로 등록된 약은 알림 시기를 변경할 수 없습니다.");
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
    
    /**
     * 투약 일정 삭제 (soft delete)
     */
    public Long deleteMedication(Long calNo, Long userNo) {
        Schedule entity = findScheduleById(calNo);



        if (entity.getMainType() != ScheduleMainType.MEDICATION) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "투약 일정이 아닙니다.");
        }

        if (!entity.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }

        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "이미 삭제된 일정입니다.");
        }

        deleteSchedule(calNo);
        return calNo;
    }
    
    /**
     * 투약 관련 메타 정보 조회 (드롭다운용)
     */
    public Map<String, List<String>> getMedicationMeta() {
        List<String> subTypes = Arrays.stream(ScheduleSubType.values())
                .filter(ScheduleSubType::isMedicationType)
                .map(Enum::name)
                .toList();
        
        List<String> frequencies = Arrays.stream(MedicationFrequency.values())
                .map(MedicationFrequency::getLabel)
                .toList();
        
        Map<String, List<String>> data = new HashMap<>();
        data.put("subTypes", subTypes);
        data.put("frequencies", frequencies);
        return data;
    }




        private List<LocalTime> defaultSlots(int times) {
        return ocrService.getDefaultTimeSlots(times);
    }








}


