package site.petful.healthservice.medical.schedule.enums;

import lombok.Getter;

@Getter
public enum ReminderOffset {
    SAME_DAY(0, "당일"),
    ONE_DAY_BEFORE(1, "1일전"),
    TWO_DAYS_BEFORE(2, "2일전"),
    THREE_DAYS_BEFORE(3, "3일전");

    private final int days;
    private final String label;

    ReminderOffset(int days, String label) {
        this.days = days;
        this.label = label;
    }

    public static ReminderOffset from(String value) {
        if (value == null) return null;
        String v = value.trim();
        for (ReminderOffset r : values()) {
            if (r.label.equals(v)) return r;
        }
        try {
            int d = Integer.parseInt(v);
            for (ReminderOffset r : values()) if (r.days == d) return r;
        } catch (NumberFormatException ignored) { }
        try { 
            return valueOf(v); 
        } catch (IllegalArgumentException ignored) { }
        return null;
    }
}


