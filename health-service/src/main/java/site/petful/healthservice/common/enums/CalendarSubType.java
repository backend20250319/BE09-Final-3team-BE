package site.petful.healthservice.common.enums;

import lombok.Getter;

@Getter
public enum CalendarSubType {
    // 투약관리 서브타입
    PILL("복용약"),
    SUPPLEMENT("영양제"),
    VACCINATION("예방접종"),
    CHECKUP("건강검진"),
    
    // 돌봄관리 서브타입
    WALK("산책"),
    GROOMING("미용"),
    BIRTHDAY("생일"),
    ETC("기타");
    
    private final String description;
    
    CalendarSubType(String description) {
        this.description = description;
    }
    
    public boolean isMedicationType() {
        return this == PILL || this == SUPPLEMENT || this == VACCINATION || this == CHECKUP;
    }
    
    public boolean isCareType() {
        return this == WALK || this == GROOMING || this == BIRTHDAY || this == ETC;
    }
}
