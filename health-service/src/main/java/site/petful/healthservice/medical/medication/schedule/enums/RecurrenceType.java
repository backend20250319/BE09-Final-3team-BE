package site.petful.healthservice.medical.medication.schedule.enums;

import lombok.Getter;

@Getter
public enum RecurrenceType {
    NONE("반복 없음"),
    DAILY("매일"),
    WEEKLY("매주"),
    MONTHLY("매월"),
    YEARLY("매년"),
    CUSTOM("사용자 정의");
    
    private final String description;
    
    RecurrenceType(String description) {
        this.description = description;
    }
}
