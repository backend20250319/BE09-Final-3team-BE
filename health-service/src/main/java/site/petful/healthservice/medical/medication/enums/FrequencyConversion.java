package site.petful.healthservice.medical.medication.enums;

import lombok.Getter;

@Getter
public enum FrequencyConversion {
    ONCE_KOREAN("한번", "1번"),
    TWICE_KOREAN("두번", "2번"),
    THREE_TIMES_KOREAN("세번", "3번"),
    ONCE_COUNT("1회", "1번"),
    TWICE_COUNT("2회", "2번"),
    THREE_TIMES_COUNT("3회", "3번");
    
    private final String from;
    private final String to;
    
    FrequencyConversion(String from, String to) {
        this.from = from;
        this.to = to;
    }
    
    // 정규식 패턴들
    public static final String DIGIT_PATTERN = "(\\d+)";
    public static final String DAILY_FREQUENCY_PATTERN = "하루(\\d+)(?:번|회)";
    public static final String WEEKLY_FREQUENCY_PATTERN = "주(?:에)?(\\d+)(?:번|회)";
    public static final String MONTHLY_FREQUENCY_PATTERN = "월(?:에)?(\\d+)(?:번|회)";

    /**
     * 변환할 문자열을 찾아서 변환된 문자열을 반환
     */
    public static String convert(String input) {
        if (input == null) return input;
        
        for (FrequencyConversion conversion : values()) {
            if (conversion.from.equals(input)) {
                return conversion.to;
            }
        }
        return input; // 변환할 수 없으면 원본 반환
    }
}
