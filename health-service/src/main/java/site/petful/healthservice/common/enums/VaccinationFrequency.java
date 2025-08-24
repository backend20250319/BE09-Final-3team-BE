package site.petful.healthservice.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum VaccinationFrequency {
    YEARLY_ONCE("연 1회", RecurrenceType.YEARLY, 1),
    HALF_YEARLY_ONCE("반년 1회", RecurrenceType.CUSTOM, 6),
    MONTHLY_ONCE("월 1회", RecurrenceType.MONTHLY, 1),
    WEEKLY_ONCE("주 1회", RecurrenceType.WEEKLY, 1);

    private final String label;
    private final RecurrenceType recurrenceType;
    private final int interval;

    VaccinationFrequency(String label, RecurrenceType recurrenceType, int interval) {
        this.label = label;
        this.recurrenceType = recurrenceType;
        this.interval = interval;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static VaccinationFrequency from(String value) {
        if (value == null) return null;
        String v = value.trim();
        for (VaccinationFrequency f : values()) {
            if (f.label.equals(v)) return f;
        }
        String upper = v.toUpperCase();
        for (VaccinationFrequency f : values()) {
            if (f.name().equals(upper)) return f;
        }
        return null;
    }

    @JsonValue
    public String jsonValue() { return name(); }
}


