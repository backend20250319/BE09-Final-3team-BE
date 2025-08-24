package site.petful.healthservice.common.enums;

import lombok.Getter;

@Getter
public enum CalendarMainType {
    MEDICATION("투약관리"),
    VACCINATION("백신관리"),
    CARE("돌봄관리");
    
    private final String description;
    
    CalendarMainType(String description) {
        this.description = description;
    }
}
