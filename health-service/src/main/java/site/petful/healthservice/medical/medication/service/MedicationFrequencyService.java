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
        // 1. 모든 공백을 먼저 제거하여 "하루에 세 번" -> "하루에세번"
        String cleanedText = text.replaceAll("\\s+", ""); 
        
        // 2. 한글 숫자-번/회 조합을 아라비아 숫자-번으로 변환
        return cleanedText.replace("한번", "1번")
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
            info.label = MedicationFrequency.DAILY_ONCE.getLabel();
            return info;
        }
        String f = normalizeText(frequency);
        Matcher mDay = DAILY_PATTERN.matcher(f);
        if (mDay.find()) {
            int times = parseIntSafe(mDay.group(1), 1);
            info.timesPerDay = times;
            info.recurrenceType = RecurrenceType.DAILY;
            info.interval = 1;
            info.label = switch (times) {
                case 1 -> MedicationFrequency.DAILY_ONCE.getLabel();
                case 2 -> MedicationFrequency.DAILY_TWICE.getLabel();
                case 3 -> MedicationFrequency.DAILY_THREE.getLabel();
                default -> MedicationFrequency.DAILY_ONCE.getLabel();
            };
            return info;
        }
        Matcher mWeek = WEEKLY_PATTERN.matcher(f);
        if (mWeek.find()) {
            info.timesPerDay = 1;
            info.recurrenceType = RecurrenceType.WEEKLY;
            info.interval = 1;
            info.label = MedicationFrequency.DAILY_ONCE.getLabel(); // 투약에서는 주/월 빈도 제거됨
            return info;
        }
        Matcher mMonth = MONTHLY_PATTERN.matcher(f);
        if (mMonth.find()) {
            info.timesPerDay = 1;
            info.recurrenceType = RecurrenceType.MONTHLY;
            info.interval = 1;
            info.label = MedicationFrequency.DAILY_ONCE.getLabel(); // 투약에서는 주/월 빈도 제거됨
            return info;
        }
        // 기본값
        info.timesPerDay = 1; 
        info.recurrenceType = RecurrenceType.DAILY; 
        info.interval = 1; 
        info.label = MedicationFrequency.DAILY_ONCE.getLabel();
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
        log.info("=== normalizeFrequency 디버깅 ===");
        log.info("원본 frequency: '{}'", frequency);
        log.info("정규화된 f: '{}'", f);
        log.info("DAILY_PATTERN: {}", DAILY_PATTERN.pattern());
        
        // 하루 N번 패턴 매칭
        Matcher mDay = DAILY_PATTERN.matcher(f);
        boolean dayMatch = mDay.find();
        log.info("DAILY 패턴 매칭 결과: {}", dayMatch);
        if (dayMatch) {
            int times = parseIntSafe(mDay.group(1), 1);
            log.info("매칭된 times: {}", times);
            String result = switch (times) {
                case 1 -> MedicationFrequency.DAILY_ONCE.getLabel();
                case 2 -> MedicationFrequency.DAILY_TWICE.getLabel();
                case 3 -> MedicationFrequency.DAILY_THREE.getLabel();
                default -> MedicationFrequency.DAILY_ONCE.getLabel();
            };
            log.info("최종 결과: {}", result);
            return result;
        }
        
        // 주에 N번 패턴 매칭 (투약에서는 주/월 빈도 제거됨)
        if (WEEKLY_PATTERN.matcher(f).find()) {
            return MedicationFrequency.DAILY_ONCE.getLabel();
        }
        
        // 월에 N번 패턴 매칭 (투약에서는 주/월 빈도 제거됨)
        if (MONTHLY_PATTERN.matcher(f).find()) {
            return MedicationFrequency.DAILY_ONCE.getLabel();
        }
        
        log.info("패턴 매칭 실패, 기본값 반환");
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
        private String label;

        public RecurrenceType getRecurrenceType() { return recurrenceType; }
        public int getInterval() { return interval; }
        public int getTimesPerDay() { return timesPerDay; }
        public String getLabel() { return label; }
        
        public void setLabel(String label) { this.label = label; }
    }
}
