package site.petful.healthservice.medical.medication.enums;

import lombok.Getter;
import site.petful.healthservice.medical.schedule.enums.RecurrenceType;

@Getter
public enum MedicationFrequency {
    DAILY_ONCE("하루에 한번", RecurrenceType.DAILY, 1, 1),
    DAILY_TWICE("하루에 두번", RecurrenceType.DAILY, 1, 2),
    DAILY_THREE_TIMES("하루에 세번", RecurrenceType.DAILY, 1, 3),
    WEEKLY_ONCE("주에 한번", RecurrenceType.WEEKLY, 1, 1),
    MONTHLY_ONCE("월에 한번", RecurrenceType.MONTHLY, 1, 1);

    private final String label;
    private final RecurrenceType recurrenceType;
    private final int interval;
    private final int timesPerDay;

    MedicationFrequency(String label, RecurrenceType recurrenceType, int interval, int timesPerDay) {
        this.label = label;
        this.recurrenceType = recurrenceType;
        this.interval = interval;
        this.timesPerDay = timesPerDay;
    }

    public static MedicationFrequency from(String value) {
        if (value == null) return null;
        String v = value.trim();
        for (MedicationFrequency mf : values()) {
            if (mf.label.equals(v)) return mf;
        }
        try { 
            return valueOf(v); 
        } catch (IllegalArgumentException ignored) { }
        return null;
    }
}


