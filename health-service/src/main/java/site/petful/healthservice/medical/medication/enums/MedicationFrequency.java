package site.petful.healthservice.medical.medication.enums;

import lombok.Getter;
import site.petful.healthservice.medical.schedule.enums.RecurrenceType;

@Getter
public enum MedicationFrequency {
    DAILY_ONCE("하루에 한 번", RecurrenceType.DAILY, 1, 1),
    DAILY_TWICE("하루에 두 번", RecurrenceType.DAILY, 1, 2),
    DAILY_THREE("하루에 세 번", RecurrenceType.DAILY, 1, 3);

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


