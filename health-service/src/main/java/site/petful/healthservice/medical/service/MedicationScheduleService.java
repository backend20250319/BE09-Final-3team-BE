package site.petful.healthservice.medical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.healthservice.common.entity.Calendar;
import site.petful.healthservice.common.enums.CalendarMainType;
import site.petful.healthservice.common.enums.CalendarSubType;
import site.petful.healthservice.common.enums.RecurrenceType;
import site.petful.healthservice.common.repository.CalendarRepository;
import site.petful.healthservice.medical.dto.PrescriptionParsedDTO;
import site.petful.healthservice.medical.dto.MedicationRequestDTO;
import site.petful.healthservice.medical.dto.MedicationResponseDTO;
import site.petful.healthservice.medical.dto.MedicationDetailDTO;
import site.petful.healthservice.medical.dto.MedicationUpdateRequestDTO;
import site.petful.healthservice.medical.dto.MedicationUpdateDiffDTO;
import site.petful.healthservice.medical.entity.CalendarMedDetail;
import site.petful.healthservice.medical.repository.CalendarMedicationDetailRepository;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.common.response.ErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationScheduleService {

    private final CalendarRepository calendarRepository;
    private final CalendarMedicationDetailRepository medicationDetailRepository;


    /**
     * 복용약/영양제 일정 생성 (캘린더 기반)
     */
    public Long createMedication(Long userNo, MedicationRequestDTO request) {
        // 직접 등록 시에는 시간들이 필수
        if (request.getTimes() == null || request.getTimes().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "일정 시간들은 필수입니다.");
        }

        int days = (request.getDurationDays() == null || request.getDurationDays() <= 0) ? 1 : request.getDurationDays();
        String freq = (request.getFrequency() == null || request.getFrequency().isBlank())
                ? (request.getAdministration() == null ? "하루 1회" : request.getAdministration())
                : request.getFrequency();

        PrescriptionParsedDTO dto = new PrescriptionParsedDTO();
        PrescriptionParsedDTO.MedicationInfo info = new PrescriptionParsedDTO.MedicationInfo();
        info.setDrugName(request.getMedicationName());
        info.setDosage(request.getDosage());
        info.setAdministration(request.getAdministration());
        info.setFrequency(freq);
        info.setPrescriptionDays(days + "일");
        info.setTimes(request.getTimes());  // 사용자가 입력한 시간들 직접 사용
        dto.setMedications(List.of(info));

        LocalDate base = (request.getStartDate() == null) ? LocalDate.now() : request.getStartDate();

        // 서브타입 매핑
        CalendarSubType subTypeEnum = (request.getSubType() != null && request.getSubType().equalsIgnoreCase("SUPPLEMENT"))
                ? CalendarSubType.SUPPLEMENT : CalendarSubType.PILL;

        List<Calendar> saved = registerMedicationSchedules(dto, userNo, base, subTypeEnum);

        // 알림 시기 반영 (기본값: 당일 알림 활성화)
        for (Calendar c : saved) {
            if (request.getReminderDaysBefore() != null) {
                c.updateReminders(List.of(request.getReminderDaysBefore()));
            } else {
                // 사용자가 알림 시기를 지정하지 않으면 기본값 (당일 알림)
                c.updateReminders(List.of(0));
            }
        }

        return saved.isEmpty() ? null : saved.get(0).getCalNo();
    }

    /**
     * 파싱된 처방전 정보를 기반으로 투약 일정을 생성/저장합니다.
     */
    public List<Calendar> registerMedicationSchedules(PrescriptionParsedDTO parsed, Long userNo, LocalDate baseDate) {
        return registerMedicationSchedules(parsed, userNo, baseDate, CalendarSubType.PILL);
    }

    /**
     * 서브타입을 지정하여 일정을 생성/저장합니다.
     */
    public List<Calendar> registerMedicationSchedules(PrescriptionParsedDTO parsed, Long userNo, LocalDate baseDate, CalendarSubType subType) {
        List<Calendar> created = new ArrayList<>();
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

            // 사용자 입력 시간이 있으면 해당 시간들 사용, 없으면 기본 시간 사용
            List<LocalTime> slots;
            if (med.getTimes() != null && !med.getTimes().isEmpty()) {
                slots = med.getTimes();  // 사용자가 입력한 시간들 직접 사용
            } else {
                slots = defaultSlots(timesPerDay);
            }

            LocalDateTime startDateTime = LocalDateTime.of(startDay, slots.get(0));
            LocalDate endDay = startDay.plusDays(Math.max(0, durationDays - 1));
            LocalDateTime endDateTime = LocalDateTime.of(endDay, slots.get(slots.size() - 1));

                    Calendar entity = Calendar.builder()
                .title(buildTitle(drugName, dosage))
                .startDate(startDateTime)
                .endDate(endDateTime)
                .mainType(CalendarMainType.MEDICATION)
                .subType(subType != null ? subType : CalendarSubType.PILL)
                .allDay(false)
                .alarmTime(startDateTime)
                .userNo(userNo)
                .recurrenceType(freqInfo.recurrenceType)
                .recurrenceInterval(freqInfo.interval)
                .recurrenceEndDate(endDateTime)
                .frequency(frequencyText)
                .times(slots)
                .reminderDaysBefore(new ArrayList<>(List.of(0)))  // 기본값: 당일 알림 활성화
                .build();

            Calendar saved = calendarRepository.save(entity);

            // 상세 정보 저장
            CalendarMedDetail detail = CalendarMedDetail.builder()
                    .calendar(saved)
                    .medicationName(drugName)
                    .dosage(dosage)
                    .durationDays(durationDays)
                    .instructions(cleanInstructions(administration))
                    .ocrRawData(parsed.getOriginalText())
                    .build();
            medicationDetailRepository.save(detail);

            created.add(saved);
        }

        return created;
    }
    
    /**
     * 투약 일정 목록 조회
     */
    public List<MedicationResponseDTO> listMedications(Long userNo, String from, String to, String subType) {
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
                .filter(c -> c.getMainType() == CalendarMainType.MEDICATION);
        if (subType != null && !subType.isBlank()) {
            stream = stream.filter(c -> c.getSubType().name().equalsIgnoreCase(subType));
        }

        return stream
                .map(c -> {
                    var detailOpt = medicationDetailRepository.findById(c.getCalNo());
                    String medName = detailOpt.map(CalendarMedDetail::getMedicationName).orElse(null);
                    String dosage = detailOpt.map(CalendarMedDetail::getDosage).orElse(null);
                    Integer durationDays = detailOpt.map(CalendarMedDetail::getDurationDays).orElse(null);
                    String instructions = detailOpt.map(CalendarMedDetail::getInstructions).orElse(null);

                    var freqInfo = parseFrequency(c.getFrequency());
                    List<LocalTime> slots = c.getTimes() != null && !c.getTimes().isEmpty() 
                        ? c.getTimes() 
                        : defaultSlots(freqInfo.getTimesPerDay());
                    
                    return MedicationResponseDTO.builder()
                            .calNo(c.getCalNo())
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
        Calendar c = calendarRepository.findById(calNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICATION_NOT_FOUND, "일정을 찾을 수 없습니다."));

        if (!c.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }
        if (Boolean.TRUE.equals(c.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "삭제된 일정입니다.");
        }
        if (c.getMainType() != CalendarMainType.MEDICATION) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "투약 일정이 아닙니다.");
        }

        var detailOpt = medicationDetailRepository.findById(calNo);
        String medName = detailOpt.map(CalendarMedDetail::getMedicationName).orElse(null);
        String dosage = detailOpt.map(CalendarMedDetail::getDosage).orElse(null);
        Integer duration = detailOpt.map(CalendarMedDetail::getDurationDays).orElse(null);
        String instructions = detailOpt.map(CalendarMedDetail::getInstructions).orElse(null);

        // 기본 시간 슬롯 사용 (상세 조회 시에는 기본 시간으로 표시)
        var freqInfo = parseFrequency(c.getFrequency());
        List<LocalTime> slots = defaultSlots(freqInfo.getTimesPerDay());

        return MedicationDetailDTO.builder()
                .calNo(c.getCalNo())
                .title(c.getTitle())
                .mainType(c.getMainType().name())
                .subType(c.getSubType().name())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .time(c.getStartDate() != null ? c.getStartDate().toLocalTime() : null)
                .times(slots)
                .frequency(c.getFrequency())
                .alarmEnabled(c.getReminderDaysBefore() != null && !c.getReminderDaysBefore().isEmpty())
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
        Calendar entity = calendarRepository.findById(calNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICATION_NOT_FOUND, "일정을 찾을 수 없습니다."));
        
        if (entity.getMainType() != CalendarMainType.MEDICATION) {
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

    private MedicationUpdateDiffDTO.Snapshot createSnapshot(Calendar entity) {
        var detailOpt = medicationDetailRepository.findById(entity.getCalNo());
        var detail = detailOpt.orElse(null);
        
        Integer reminder = (entity.getReminderDaysBefore() == null || entity.getReminderDaysBefore().isEmpty())
                ? null : entity.getReminderDaysBefore().get(0);
        
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

    private void updateMedicationSchedule(Calendar entity, MedicationUpdateRequestDTO request) {
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
            (medicationDetailRepository.findById(entity.getCalNo()).map(d -> d.getInstructions()).orElse(null));
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
            entity.updateReminders(new ArrayList<>(List.of(request.getReminderDaysBefore())));
        }

        // 엔티티 저장
        calendarRepository.save(entity);

        // 상세 정보 업데이트
        updateMedicationDetail(entity.getCalNo(), request);
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
    public Long toggleAlarm(Long calNo, Long userNo, boolean enabled) {
        Calendar entity = calendarRepository.findById(calNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICATION_NOT_FOUND, "일정을 찾을 수 없습니다."));

        if (entity.getMainType() != CalendarMainType.MEDICATION) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "투약 일정이 아닙니다.");
        }

        if (!entity.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        }
        if (Boolean.TRUE.equals(entity.getDeleted())) {
            throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "삭제된 일정입니다.");
        }

        boolean isOn = entity.getReminderDaysBefore() != null && !entity.getReminderDaysBefore().isEmpty();
        if (enabled) {
            if (isOn) throw new BusinessException(ErrorCode.ALARM_ALREADY_ENABLED);
            entity.updateReminders(new ArrayList<>(List.of(0)));
        } else {
            if (!isOn) throw new BusinessException(ErrorCode.ALARM_ALREADY_DISABLED);
            entity.updateReminders(new ArrayList<>(List.of()));
        }

        calendarRepository.save(entity);
        return entity.getCalNo();
    }
    
    /**
     * 투약 일정 삭제 (soft delete)
     */
    public Long deleteMedication(Long calNo, Long userNo) {
        Calendar entity = calendarRepository.findById(calNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICATION_NOT_FOUND, "일정을 찾을 수 없습니다."));


        if (entity.getMainType() != CalendarMainType.MEDICATION) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "투약 일정이 아닙니다.");
        }

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
    
    /**
     * 투약 관련 메타 정보 조회 (드롭다운용)
     */
    public java.util.Map<String, java.util.List<String>> getMedicationMeta() {
        java.util.List<String> subTypes = java.util.Arrays.stream(CalendarSubType.values())
                .filter(CalendarSubType::isMedicationType)
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
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
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
}


