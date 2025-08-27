package site.petful.healthservice.medical.medication.schedule.enums;

/**
 * 스케줄 서브타입 열거형
 * 각 서브타입은 특정 카테고리의 일정을 나타냄
 */
public enum ScheduleSubType {
    // 돌봄 관련 서브타입
    WALK("산책"),
    BIRTHDAY("생일"),
    GROOMING("미용"),
    ETC("기타"),

    // 접종 관련 서브타입
    VACCINE("접종"),
    CHECKUP("건강검진"),

    // 투약 관련 서브타입
    PILL("복용약"),
    SUPPLEMENT("영양제");

    private final String label;

    ScheduleSubType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 접종 관련 서브타입인지 확인
     */
    public boolean isVaccinationType() {
        return this == VACCINE || this == CHECKUP;
    }

    /**
     * 투약 관련 서브타입인지 확인
     */
    public boolean isMedicationType() {
        return this == PILL || this == SUPPLEMENT;
    }

    /**
     * 돌봄 관련 서브타입인지 확인
     */
    public boolean isCareType() {
        return this == WALK || this == GROOMING || this == BIRTHDAY || this == ETC;
    }
}
