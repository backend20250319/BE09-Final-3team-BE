package site.petful.healthservice.medical.medication.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.petful.healthservice.medical.medication.enums.FrequencyConversion;
import site.petful.healthservice.medical.medication.enums.MedicationFrequency;
import site.petful.healthservice.medical.schedule.enums.RecurrenceType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class MedicationFrequencyService {
    
    // 정규식 패턴 캐싱 (성능 최적화)
    private static final Pattern DAILY_PATTERN = Pattern.compile(FrequencyConversion.DAILY_FREQUENCY_PATTERN);
    private static final Pattern WEEKLY_PATTERN = Pattern.compile(FrequencyConversion.WEEKLY_FREQUENCY_PATTERN);
    private static final Pattern MONTHLY_PATTERN = Pattern.compile(FrequencyConversion.MONTHLY_FREQUENCY_PATTERN);
    
    /**
     * 공통 텍스트 정규화 메서드
     */
    public String normalizeText(String text) {
        if (text == null) return text;
        return text.replaceAll("\\s+", "")
                   .replace("한번", "1번")
                   .replace("두번", "2번")
                   .replace("세번", "3번")
                   .replace("1회", "1번")
                   .replace("2회", "2번")
                   .replace("3회", "3번");
    }
    
    /**
     * 빈도 텍스트를 FrequencyInfo 객체로 변환
     */
    public FrequencyInfo parseFrequency(String frequency) {
        // 지원: 하루 N회, 주에 1번, 주에 N번, 월에 1번
        FrequencyInfo info = new FrequencyInfo();
        if (frequency == null) {
            info.recurrenceType = RecurrenceType.DAILY; 
            info.interval = 1; 
            info.timesPerDay = 1; 
            return info;
        }
        String f = normalizeText(frequency);
        Matcher mDay = DAILY_PATTERN.matcher(f);
        if (mDay.find()) {
            info.timesPerDay = parseIntSafe(mDay.group(1), 1);
            info.recurrenceType = RecurrenceType.DAILY;
            info.interval = 1;
            return info;
        }
        Matcher mWeek = WEEKLY_PATTERN.matcher(f);
        if (mWeek.find()) {
            info.timesPerDay = 1;
            info.recurrenceType = RecurrenceType.WEEKLY;
            info.interval = 1;
            return info;
        }
        Matcher mMonth = MONTHLY_PATTERN.matcher(f);
        if (mMonth.find()) {
            info.timesPerDay = 1;
            info.recurrenceType = RecurrenceType.MONTHLY;
            info.interval = 1;
            return info;
        }
        // 기본값
        info.timesPerDay = 1; 
        info.recurrenceType = RecurrenceType.DAILY; 
        info.interval = 1; 
        return info;
    }
    
    /**
     * 빈도를 표준화된 한글 라벨로 변환 (프론트엔드 요구사항에 맞춤)
     */
    public String normalizeFrequency(String frequency) {
        if (frequency == null || frequency.trim().isEmpty()) {
            return MedicationFrequency.DAILY_ONCE.getLabel();
        }
        
        String f = normalizeText(frequency);
        
        // 하루 N번 패턴 매칭
        Matcher mDay = DAILY_PATTERN.matcher(f);
        if (mDay.find()) {
            int times = parseIntSafe(mDay.group(1), 1);
            return switch (times) {
                case 1 -> MedicationFrequency.DAILY_ONCE.getLabel();
                case 2 -> MedicationFrequency.DAILY_TWICE.getLabel();
                case 3 -> MedicationFrequency.DAILY_THREE_TIMES.getLabel();
                default -> MedicationFrequency.DAILY_ONCE.getLabel();
            };
        }
        
        // 주에 N번 패턴 매칭
        if (WEEKLY_PATTERN.matcher(f).find()) {
            return MedicationFrequency.WEEKLY_ONCE.getLabel();
        }
        
        // 월에 N번 패턴 매칭
        if (MONTHLY_PATTERN.matcher(f).find()) {
            return MedicationFrequency.MONTHLY_ONCE.getLabel();
        }
        
        return MedicationFrequency.DAILY_ONCE.getLabel(); // 기본값
    }
    
    private int parseIntSafe(String s, int def) {
        try { 
            return Integer.parseInt(s); 
        } catch (NumberFormatException e) { 
            log.debug("숫자 파싱 실패, 기본값 사용: {} -> {}", s, def);
            return def; 
        }
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
