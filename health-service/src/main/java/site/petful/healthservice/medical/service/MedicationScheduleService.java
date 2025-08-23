package site.petful.healthservice.medical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.petful.healthservice.common.entity.Calendar;
import site.petful.healthservice.common.enums.CalendarMainType;
import site.petful.healthservice.common.enums.CalendarSubType;
import site.petful.healthservice.common.enums.RecurrenceType;
import site.petful.healthservice.common.repository.CalendarRepository;
import site.petful.healthservice.medical.dto.PrescriptionParsedDTO;

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

            int timesPerDay = extractTimesPerDay(frequencyText);
            if (timesPerDay <= 0) timesPerDay = 1;

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
                    .subType(CalendarSubType.PILL)
                    .allDay(false)
                    .description(administration)
                    .alarmTime(startDateTime)
                    .userNo(userNo)
                    .recurrenceType(RecurrenceType.DAILY)
                    .recurrenceInterval(1)
                    .recurrenceEndDate(endDateTime)
                    .medicationName(drugName)
                    .dosage(dosage)
                    .frequency(frequencyText)
                    .durationDays(durationDays)
                    .instructions(administration)
                    .ocrRawData(parsed.getOriginalText())
                    .build();

            created.add(calendarRepository.save(entity));
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

    private int extractTimesPerDay(String frequency) {
        if (frequency == null) return 0;
        // "하루 2회", "하루2회" 등 지원
        Matcher m = Pattern.compile("하루\\s*(\\d+)회").matcher(frequency);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException ignored) { }
        }
        return 0;
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
}


