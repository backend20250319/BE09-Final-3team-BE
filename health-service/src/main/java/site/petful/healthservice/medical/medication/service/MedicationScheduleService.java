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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MedicationScheduleService extends AbstractScheduleService {

    private final ScheduleMedicationDetailRepository medicationDetailRepository;
    private final PetServiceClient petServiceClient;

    public MedicationScheduleService(ScheduleRepository scheduleRepository, 
                                   ScheduleMedicationDetailRepository medicationDetailRepository,
                                   PetServiceClient petServiceClient) {
        super(scheduleRepository);
        this.medicationDetailRepository = medicationDetailRepository;
        this.petServiceClient = petServiceClient;
    }


    /**
     * 복용약/영양제 일정 생성 (캘린더 기반)
     */
    public Long createMedication(Long userNo, @Valid MedicationRequestDTO request) {
        // 펫 소유권 검증
        if (!isPetOwnedByUser(request.getPetNo(), userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 펫에 대한 접근 권한이 없습니다.");
        }

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
                .dosage(request.getAmount())
                .instructions(request.getInstruction())
                .durationDays(request.getDurationDays())
                .build();

        medicationDetailRepository.save(detail);

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
        // 펫 소유권 검증
        if (!isPetOwnedByUser(petNo, userNo)) {
            throw new BusinessException(ErrorCode.PET_OWNERSHIP_VERIFICATION_FAILED, "해당 펫에 대한 접근 권한이 없습니다.");
        }
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
            detail.setInstructions(cleanInstructions(administration));
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
        // 펫 소유권 검증
        if (!isPetOwnedByUser(petNo, userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 펫에 대한 접근 권한이 없습니다.");
        }

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
                    String instructions = detailOpt.map(ScheduleMedDetail::getInstructions).orElse(null);

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
                            .instructions(instructions)
                            .time(c.getStartDate() != null ? c.getStartDate().toLocalTime() : null)
                            .times(slots)
                            .build();
                })
                .toList();
    }

    /**
     * 투약 일정 상세 조회
     */
    public MedicationDetailDTO getMedicationDetail(Long calNo, Long userNo) {
        Schedule c = findScheduleById(calNo);

        // 펫 소유권 검증
        if (!isPetOwnedByUser(c.getPetNo(), userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 펫에 대한 접근 권한이 없습니다.");
        }

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
        String instructions = detailOpt.map(ScheduleMedDetail::getInstructions).orElse(null);

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
                .medicationName(medName)
                .dosage(dosage)
                .durationDays(duration)
                .instructions(instructions)
                .build();
    }
    
    /**
     * 투약 일정 수정 (부분 업데이트)
     */
    public MedicationUpdateDiffDTO updateMedication(Long calNo, MedicationUpdateRequestDTO request, Long userNo) {
        // 조회 및 소유자 검증
        Schedule entity = findScheduleById(calNo);
        
        // 펫 소유권 검증
        if (!isPetOwnedByUser(entity.getPetNo(), userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 펫에 대한 접근 권한이 없습니다.");
        }
        
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
                .instructions(detail != null ? detail.getInstructions() : null)
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
        String admin = request.getAdministration() != null ? request.getAdministration() : 
            (medicationDetailRepository.findById(entity.getScheduleNo()).map(d -> d.getInstructions()).orElse(null));
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
            if (request.getAdministration() != null) detail.setInstructions(request.getAdministration());
            medicationDetailRepository.save(detail);
        }
    }
    
    /**
     * 투약 일정 알림 on/off
     */
    public Boolean toggleAlarm(Long calNo, Long userNo) {
        Schedule entity = findScheduleById(calNo);

        // 펫 소유권 검증
        if (!isPetOwnedByUser(entity.getPetNo(), userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 펫에 대한 접근 권한이 없습니다.");
        }

        if (entity.getMainType() != ScheduleMainType.MEDICATION) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "투약 일정이 아닙니다.");
        }

        if (!entity.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }
        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "삭제된 일정입니다.");
        }

        // 현재 알림 상태를 반대로 토글
        boolean currentAlarmEnabled = entity.getReminderDaysBefore() != null;
        boolean newAlarmState = !currentAlarmEnabled;
        
        // AbstractScheduleService의 공통 메서드 사용
        super.toggleAlarm(calNo, newAlarmState);
        
        return newAlarmState;
    }
    
    /**
     * 투약 일정 삭제 (soft delete)
     */
    public Long deleteMedication(Long calNo, Long userNo) {
        Schedule entity = findScheduleById(calNo);

        // 펫 소유권 검증
        if (!isPetOwnedByUser(entity.getPetNo(), userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 펫에 대한 접근 권한이 없습니다.");
        }

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
    public java.util.Map<String, java.util.List<String>> getMedicationMeta() {
        java.util.List<String> subTypes = java.util.Arrays.stream(ScheduleSubType.values())
                .filter(ScheduleSubType::isMedicationType)
                .map(Enum::name)
                .toList();
        
        java.util.List<String> frequencies = java.util.List.of("하루 1회", "하루 2회", "하루 3회", "주에 1번", "월에 1번");
        
        java.util.Map<String, java.util.List<String>> data = new java.util.HashMap<>();
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



    private String cleanInstructions(String administration) {
        if (administration == null) return null;
        
        // "하루 N회" 패턴을 정확하게 제거
        String cleaned = administration
            .replaceAll("하루\\s*\\d+회", "")  // "하루 2회", "하루1회" 등 제거
            .replaceAll("하루\\s*\\d+번", "")  // "하루 2번", "하루2번" 등 제거
            .replaceAll("\\s*,\\s*", "")       // 쉼표와 공백 제거
            .replaceAll("^\\s+|\\s+$", "")     // 앞뒤 공백 제거
            .replaceAll("\\s+", " ");          // 연속된 공백을 하나로
        
        // 빈 문자열이면 null 반환
        return cleaned.isEmpty() ? null : cleaned;
    }

    public static class FrequencyInfo {
        private RecurrenceType recurrenceType = RecurrenceType.DAILY;
        private int interval = 1;
        private int timesPerDay = 1;

        public RecurrenceType getRecurrenceType() { return recurrenceType; }
        public int getInterval() { return interval; }
        public int getTimesPerDay() { return timesPerDay; }
    }

    private boolean isPetOwnedByUser(Long petNo, Long userNo) {
        try {
            ApiResponse<PetResponse> response = petServiceClient.getPet(petNo);
            
            if (response != null && response.getData() != null) {
                PetResponse pet = response.getData();
                if (pet.getUserNo() != null) {
                    return pet.getUserNo().equals(userNo);
                }
            }
            return false;
            
        } catch (Exception e) {
            log.error("펫 소유권 검증 중 예외 발생: petNo={}, userNo={}", petNo, userNo, e);
            return false;
        }
    }
}


