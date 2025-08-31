package site.petful.healthservice.activity.enums;

import lombok.Getter;

@Getter
public enum ActivityLevel {
    LOW(1.2, "거의 안 움직여요"),
    MEDIUM_LOW(1.5, "가끔 산책해요"),
    MEDIUM_HIGH(1.7, "자주 뛰어놀아요"),
    HIGH(1.9, "매우 활동적이에요");

    private final double value;
    private final String label;

    ActivityLevel(double value, String label) {
        this.value = value;
        this.label = label;
    }

    public static ActivityLevel from(double value) {
        for (ActivityLevel level : values()) {
            if (level.value == value) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid activity level: " + value);
    }

    public static ActivityLevel from(String label) {
        for (ActivityLevel level : values()) {
            if (level.label.equals(label)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Invalid activity level label: " + label);
    }
}
