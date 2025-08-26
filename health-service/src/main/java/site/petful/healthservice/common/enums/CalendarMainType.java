package site.petful.healthservice.common.enums;

import lombok.Getter;

@Getter
public enum CalendarMainType {
    CARE("돌봄"),
    VACCINATION("접종"),
    MEDICATION("투약");

    private final String description;

    CalendarMainType(String description) {
        this.description = description;
    }
}
