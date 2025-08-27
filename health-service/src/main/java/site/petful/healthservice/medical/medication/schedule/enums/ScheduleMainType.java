package site.petful.healthservice.medical.medication.schedule.enums;

import lombok.Getter;

@Getter
public enum ScheduleMainType {
    CARE("돌봄"),
    VACCINATION("접종"),
    MEDICATION("투약"),
    EXERCISE("운동"),
    TRAINING("훈련");

    private final String description;

    ScheduleMainType(String description) {
        this.description = description;
    }
}
