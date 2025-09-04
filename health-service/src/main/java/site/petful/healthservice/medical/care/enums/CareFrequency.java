package site.petful.healthservice.medical.care.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import site.petful.healthservice.medical.schedule.enums.RecurrenceType;

@Getter
public enum CareFrequency {
    DAILY("매일", RecurrenceType.DAILY, 1),
    WEEKLY("매주", RecurrenceType.WEEKLY, 1),
    MONTHLY("매월", RecurrenceType.MONTHLY, 1),
    SINGLE_DAY("당일", RecurrenceType.DAILY, 1);

    private final String label;
    private final RecurrenceType recurrenceType;
    private final int interval;

    CareFrequency(String label, RecurrenceType recurrenceType, int interval) {
        this.label = label;
        this.recurrenceType = recurrenceType;
        this.interval = interval;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CareFrequency from(String value) {
        if (value == null) return null;
        String v = value.trim();
        // match by label (한글)
        for (CareFrequency cf : values()) {
            if (cf.label.equals(v)) return cf;
        }
        // match by enum name, case-insensitive
        String upper = v.toUpperCase();
        for (CareFrequency cf : values()) {
            if (cf.name().equals(upper)) return cf;
        }
        return null;
    }

    @JsonValue
    public String jsonValue() {
        return label;  // name() 대신 label(한국어) 반환
    }
}


