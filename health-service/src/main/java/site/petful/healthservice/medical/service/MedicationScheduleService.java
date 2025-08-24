package site.petful.healthservice.medical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.healthservice.common.entity.Calendar;
import site.petful.healthservice.common.enums.CalendarMainType;
import site.petful.healthservice.common.enums.CalendarSubType;
import site.petful.healthservice.common.enums.RecurrenceType;
import site.petful.healthservice.common.repository.CalendarRepository;
import site.petful.healthservice.medical.dto.PrescriptionParsedDTO;
import site.petful.healthservice.medical.entity.CalendarMedDetail;
import site.petful.healthservice.medical.repository.CalendarMedicationDetailRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MedicationScheduleService {

    private final CalendarRepository calendarRepository;
    private final CalendarMedicationDetailRepository medicationDetailRepository;

    public CalendarRepository getCalendarRepository() { return calendarRepository; }

    /**
     * 파싱된 처방전 정보를 기반으로 투약 일정을 생성/저장합니다.
     * - main_type: MEDICATION
     * - sub_type: PILL
     * - 빈도 "하루 N회" 기준 기본 시간 슬롯 설정 (식후 +30분 보정)
     * - 반복: DAILY, interval=1, 종료일 = 시작일 + (처방일수-1)
     */
    public List<Calendar> registerMedicationSchedules(PrescriptionParsedDTO parsed,
                                                      Long userNo,
                                                      LocalDate baseDate) {
        return registerMedicationSchedules(parsed, userNo, baseDate, CalendarSubType.PILL);
    }

    /**
     * 서브타입을 지정하여 일정을 생성/저장합니다.
     */
    public List<Calendar> registerMedicationSchedules(PrescriptionParsedDTO parsed,
                                                      Long userNo,
                                                      LocalDate baseDate,
                                                      CalendarSubType subType) {
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

            List<LocalTime> slots = defaultSlots(timesPerDay);
            if (isPostMeal(administration)) {
                slots = addMinutes(slots, 30);
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
                    .description(administration)
                    .alarmTime(startDateTime)
                    .userNo(userNo)
                    .recurrenceType(freqInfo.recurrenceType)
                    .recurrenceInterval(freqInfo.interval)
                    .recurrenceEndDate(endDateTime)
                    // Keep frequency at calendar level for cross-domain consistency
                    .frequency(frequencyText)
                    // Keep legacy medication fields for backward compatibility during migration
                    .medicationName(drugName)
                    .dosage(dosage)
                    .durationDays(durationDays)
                    .instructions(administration)
                    .ocrRawData(parsed.getOriginalText())
                    .build();

            Calendar saved = calendarRepository.save(entity);

            // save detail
            CalendarMedDetail detail = CalendarMedDetail.builder()
                    .calNo(saved.getCalNo())
                    .calendar(saved)
                    .medicationName(drugName)
                    .dosage(dosage)
                    .durationDays(durationDays)
                    .instructions(administration)
                    .ocrRawData(parsed.getOriginalText())
                    .build();
            medicationDetailRepository.save(detail);

            created.add(saved);
        }

        return created;
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
            // 주간 빈도: interval은 해당 주기(1주), timesPerDay는 1로 간주
            info.timesPerDay = 1;
            info.recurrenceType = RecurrenceType.WEEKLY;
            info.interval = 1; // 캘린더 엔티티는 개별 등록이므로 interval=1, 실제 N번은 UI가 요일 지정 시 분할 생성으로 처리 예정
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

    // 아래 공개 메서드들은 컨트롤러의 부분 업데이트 계산에 재사용합니다.
    public FrequencyInfo parseFrequencyPublic(String f) { return parseFrequency(f); }
    public java.util.List<java.time.LocalTime> getDefaultSlotsPublic(int times) { return defaultSlots(times); }
    public java.util.List<java.time.LocalTime> addMinutesPublic(java.util.List<java.time.LocalTime> src, int minutes) { return addMinutes(src, minutes); }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private boolean isPostMeal(String administration) {
        if (administration == null) return false;
        return administration.contains("식후");
    }

    private List<LocalTime> defaultSlots(int times) {
        List<LocalTime> list = new ArrayList<>();
        switch (times) {
            case 1 -> list.add(LocalTime.of(9, 0));
            case 2 -> { list.add(LocalTime.of(9, 0)); list.add(LocalTime.of(21, 0)); }
            case 3 -> { list.add(LocalTime.of(8, 0)); list.add(LocalTime.of(14, 0)); list.add(LocalTime.of(20, 0)); }
            default -> list.add(LocalTime.of(9, 0));
        }
        return list;
    }

    private List<LocalTime> addMinutes(List<LocalTime> src, int minutes) {
        List<LocalTime> out = new ArrayList<>(src.size());
        for (LocalTime t : src) out.add(t.plusMinutes(minutes));
        return out;
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


