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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import site.petful.healthservice.connectNotice.dto.EventMessage;

@Slf4j
@Service
public class MedicationScheduleService extends AbstractScheduleService {

    private final ScheduleMedicationDetailRepository medicationDetailRepository;
    private final PetServiceClient petServiceClient;
    private final RabbitTemplate rabbitTemplate;

    public MedicationScheduleService(ScheduleRepository scheduleRepository, 
                                   ScheduleMedicationDetailRepository medicationDetailRepository,
                                   PetServiceClient petServiceClient,
                                   RabbitTemplate rabbitTemplate) {
        super(scheduleRepository);
        this.medicationDetailRepository = medicationDetailRepository;
        this.petServiceClient = petServiceClient;
        this.rabbitTemplate = rabbitTemplate;
    }


    /**
     * 복용약/영양제 일정 생성 (캘린더 기반)
     */
    public Long createMedication(Long userNo, @Valid MedicationRequestDTO request) {


        // 공통 DTO로 변환하여 상속된 공통 로직 활용
        ScheduleRequestDTO commonRequest = ScheduleRequestDTO.builder()
                .petNo(request.getPetNo())
                .title(request.getName())  // 약 이름을 title로 사용
                .startDate(request.getStartDate())
                .endDate(request.getStartDate().plusDays(request.getDurationDays() - 1))
                .subType(ScheduleSubType.PILL)  // 약의 경우 기본값으로 PILL 사용
                .times(request.getTimes())
                .frequency(RecurrenceType.DAILY)  // 약의 경우 기본값으로 DAILY 사용
                .recurrenceInterval(1)
                .recurrenceEndDate(request.getStartDate().plusDays(request.getDurationDays() - 1))
                .reminderDaysBefore(request.getReminderDaysBefore())
                .frequencyText(request.getMedicationFrequency().getLabel())
                .build();

        // 상속된 공통 서비스 사용
        Schedule entity = createScheduleEntity(userNo, commonRequest, ScheduleMainType.MEDICATION);
        Long scheduleNo = saveSchedule(entity);

        // 약 관련 상세 정보 저장
        ScheduleMedDetail detail = ScheduleMedDetail.builder()
                .scheduleNo(scheduleNo)
                .medicationName(request.getName())
                .dosage("")                    // 빈 문자열로 설정
                .durationDays(request.getDurationDays())
                .isPrescription(false)         // 수동 등록은 일반 등록
                .build();

        medicationDetailRepository.save(detail);
        
        // ScheduleMedDetail 저장 후 스케줄 생성 이벤트 발행
        publishScheduleCreatedEvent(entity);

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

            int durationDays = extractDays(med.getPrescriptionDays());
            if (durationDays <= 0) durationDays = 1;

            FrequencyInfo freqInfo = parseFrequency(frequencyText);
            int timesPerDay = freqInfo.timesPerDay > 0 ? freqInfo.timesPerDay : 1;

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
                .title(buildTitle(drugName, dosage))
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
            publishScheduleCreatedEvent(savedSchedule);
            
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
    public List<MedicationResponseDTO> listMedications(Long userNo, Long petNo, String from, String to, String subType) {


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

        List<Schedule> items = scheduleRepository.findByUserNoAndDateRange(userNo, start, end);

        var stream = items.stream()
                .filter(c -> c.getMainType() == ScheduleMainType.MEDICATION)
                .filter(c -> c.getPetNo().equals(petNo)); // 특정 펫의 일정만 필터링
        if (subType != null && !subType.isBlank()) {
            stream = stream.filter(c -> c.getSubType().name().equalsIgnoreCase(subType));
        }

        return stream
                .map(c -> {
                    var detailOpt = medicationDetailRepository.findById(c.getScheduleNo());
                    String medName = detailOpt.map(ScheduleMedDetail::getMedicationName).orElse(null);
                    String dosage = detailOpt.map(ScheduleMedDetail::getDosage).orElse(null);
                    Integer durationDays = detailOpt.map(ScheduleMedDetail::getDurationDays).orElse(null);
                    Boolean isPrescription = detailOpt.map(ScheduleMedDetail::getIsPrescription).orElse(false);

                    var freqInfo = parseFrequency(c.getFrequency());
                    List<LocalTime> slots = c.getTimesAsList() != null && !c.getTimesAsList().isEmpty() 
                        ? c.getTimesAsList() 
                        : defaultSlots(freqInfo.getTimesPerDay());
                    
                    return MedicationResponseDTO.builder()
                            .scheduleNo(c.getScheduleNo())
                            .title(c.getTitle())
                            .startDate(c.getStartDate())
                            .endDate(c.getEndDate())
                            .mainType(c.getMainType().name())
                            .subType(c.getSubType().name())
                            .medicationName(medName)
                            .dosage(dosage)
                            .frequency(c.getFrequency())
                            .durationDays(durationDays)
                            .time(c.getStartDate() != null ? c.getStartDate().toLocalTime() : null)
                            .times(slots)
                            .reminderDaysBefore(c.getReminderDaysBefore())
                            .lastReminderDaysBefore(c.getLastReminderDaysBefore())
                            .isPrescription(isPrescription)
                            .build();
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

        var detailOpt = medicationDetailRepository.findById(calNo);
        String medName = detailOpt.map(ScheduleMedDetail::getMedicationName).orElse(null);
        String dosage = detailOpt.map(ScheduleMedDetail::getDosage).orElse(null);
        Integer duration = detailOpt.map(ScheduleMedDetail::getDurationDays).orElse(null);
        Boolean isPrescription = detailOpt.map(ScheduleMedDetail::getIsPrescription).orElse(false);

        // 기본 시간 슬롯 사용 (상세 조회 시에는 기본 시간으로 표시)
        var freqInfo = parseFrequency(c.getFrequency());
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
        var detailOpt = medicationDetailRepository.findById(entity.getScheduleNo());
        var detail = detailOpt.orElse(null);
        
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

        // 기본값 설정
        LocalDate base = request.getStartDate() != null ? request.getStartDate() : entity.getStartDate().toLocalDate();
        Integer duration = request.getDurationDays() != null ? request.getDurationDays() : 
            (entity.getStartDate() != null && entity.getEndDate() != null ? 
                (int) java.time.temporal.ChronoUnit.DAYS.between(entity.getStartDate().toLocalDate(), entity.getEndDate().toLocalDate()) + 1 : 1);
        // administration 필드는 더 이상 사용하지 않음
        String freq = request.getFrequency() != null ? request.getFrequency() : entity.getFrequency();

        // 빈도/시간 재계산
        var freqInfo = parseFrequency(freq);
        List<LocalTime> slots;
        if (request.getTimes() != null && !request.getTimes().isEmpty()) {
            slots = request.getTimes();  // 사용자가 입력한 시간들 직접 사용
        } else {
            slots = defaultSlots(freqInfo.getTimesPerDay());  // 기본 시간 사용
        }

        LocalDateTime startDt = LocalDateTime.of(base, slots.get(0));
        LocalDate endDay = base.plusDays(Math.max(0, duration - 1));
        LocalDateTime endDt = LocalDateTime.of(endDay, slots.get(slots.size() - 1));

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
        var detailOpt = medicationDetailRepository.findById(calNo);
        if (detailOpt.isPresent()) {
            var detail = detailOpt.get();
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
        var detailOpt = medicationDetailRepository.findById(calNo);
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
        
        List<String> frequencies = List.of("하루에 한 번", "하루에 두 번", "하루에 세 번", "주에 한 번", "월에 한 번");
        
        Map<String, List<String>> data = new HashMap<>();
        data.put("subTypes", subTypes);
        data.put("frequencies", frequencies);
        return data;
    }


    private String buildTitle(String drugName, String dosage) {
        if (drugName == null && dosage == null) return "투약";
        if (drugName == null) return dosage;
        if (dosage == null) return drugName;
        return drugName + " " + dosage;
    }

    private int extractDays(String daysText) {
        if (daysText == null) return 0;
        Matcher m = Pattern.compile("(\\d+)").matcher(daysText);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException ignored) { }
        }
        return 0;
    }

    private FrequencyInfo parseFrequency(String frequency) {
        // 지원: 하루 N회, 주에 1번, 주에 N번, 월에 1번
        FrequencyInfo info = new FrequencyInfo();
        if (frequency == null) {
            info.recurrenceType = RecurrenceType.DAILY; info.interval = 1; info.timesPerDay = 1; return info;
        }
        String f = frequency.replaceAll("\\s+", "");
        f = f.replace("한번", "1번").replace("두번", "2번").replace("세번", "3번");
        f = f.replace("1회", "1번").replace("2회", "2번").replace("3회", "3번");
        Matcher mDay = Pattern.compile("하루(\\d+)번").matcher(f);
        if (mDay.find()) {
            info.timesPerDay = parseIntSafe(mDay.group(1), 1);
            info.recurrenceType = RecurrenceType.DAILY;
            info.interval = 1;
            return info;
        }
        Matcher mWeek = Pattern.compile("주에(\\d+)번").matcher(f);
        if (mWeek.find()) {
            info.timesPerDay = 1;
            info.recurrenceType = RecurrenceType.WEEKLY;
            info.interval = 1;
            return info;
        }
        Matcher mMonth = Pattern.compile("월에(\\d+)번").matcher(f);
        if (mMonth.find()) {
            info.timesPerDay = 1;
            info.recurrenceType = RecurrenceType.MONTHLY;
            info.interval = 1;
            return info;
        }
        // 기본값
        info.timesPerDay = 1; info.recurrenceType = RecurrenceType.DAILY; info.interval = 1; return info;
    }

    private int parseIntSafe(String s, int def) {
        try { 
            return Integer.parseInt(s); 
        } catch (NumberFormatException e) { 
            log.debug("숫자 파싱 실패, 기본값 사용: {} -> {}", s, def);
            return def; 
        }
    }



    private List<LocalTime> defaultSlots(int times) {
        List<LocalTime> list = new ArrayList<>();
        switch (times) {
            case 1 -> list.add(LocalTime.of(9, 0));  // 09:00
            case 2 -> { 
                list.add(LocalTime.of(8, 0));   // 아침 08:00
                list.add(LocalTime.of(20, 0));  // 저녁 20:00
            }
            case 3 -> { 
                list.add(LocalTime.of(8, 0));   // 아침 08:00
                list.add(LocalTime.of(12, 0));  // 점심 12:00
                list.add(LocalTime.of(20, 0));  // 저녁 20:00
            }
            default -> list.add(LocalTime.of(9, 0));
        }
        return list;
    }





    public static class FrequencyInfo {
        private RecurrenceType recurrenceType = RecurrenceType.DAILY;
        private int interval = 1;
        private int timesPerDay = 1;

        public RecurrenceType getRecurrenceType() { return recurrenceType; }
        public int getInterval() { return interval; }
        public int getTimesPerDay() { return timesPerDay; }
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


