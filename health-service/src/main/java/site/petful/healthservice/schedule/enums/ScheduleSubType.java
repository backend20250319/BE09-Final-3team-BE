package site.petful.healthservice.schedule.enums;

import lombok.Getter;

@Getter
public enum ScheduleSubType {
    // 돌봄 서브타입 (CARE 메인타입)
    WALK("산책"),
    GROOMING("미용"),
    BIRTHDAY("생일"),
    ETC("기타"),
    
    // 접종 서브타입 (VACCINATION 메인타입)
    VACCINATION("예방접종"),
    CHECKUP("건강검진"),
    
    // 투약 서브타입 (MEDICATION 메인타입)
    PILL("복용약"),
    SUPPLEMENT("영양제"),
    
    // 운동 서브타입 (EXERCISE 메인타입)
    RUNNING("달리기"),
    SWIMMING("수영"),
    PLAY("놀이"),
    
    // 훈련 서브타입 (TRAINING 메인타입)
    BASIC_TRAINING("기본훈련"),
    ADVANCED_TRAINING("고급훈련"),
    AGILITY("애질리티");
    
    private final String description;
    
    ScheduleSubType(String description) {
        this.description = description;
    }
    
    public boolean isCareType() {
        return this == WALK || this == GROOMING || this == BIRTHDAY || this == ETC;
    }
    
    public boolean isVaccinationType() {
        return this == VACCINATION || this == CHECKUP;
    }
    
    public boolean isMedicationType() {
        return this == PILL || this == SUPPLEMENT;
    }
    
    public boolean isExerciseType() {
        return this == RUNNING || this == SWIMMING || this == PLAY;
    }
    
    public boolean isTrainingType() {
        return this == BASIC_TRAINING || this == ADVANCED_TRAINING || this == AGILITY;
    }
}
