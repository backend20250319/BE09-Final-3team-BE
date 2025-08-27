package site.petful.healthservice.schedule.enums;

/**
 * 스케줄 서브타입 열거형
 * 각 서브타입은 특정 카테고리의 일정을 나타냄
 */
public enum ScheduleSubType {
    // 돌봄 관련 서브타입
    WALK("산책"),
    GROOMING("그루밍"),
    BIRTHDAY("생일"),
    ETC("기타"),
    
    // 접종 관련 서브타입
    VACCINE("접종"),
    BOOSTER("부스터"),
    
    // 투약 관련 서브타입
    PILL("약"),
    INJECTION("주사"),
    DROPS("점안/점이"),
    
    // 건강 관련 서브타입
    CHECKUP("검진"),
    TREATMENT("치료"),
    SURGERY("수술");

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
        return this == VACCINE || this == BOOSTER;
    }

    /**
     * 투약 관련 서브타입인지 확인
     */
    public boolean isMedicationType() {
        return this == PILL || this == INJECTION || this == DROPS;
    }

    /**
     * 돌봄 관련 서브타입인지 확인
     */
    public boolean isCareType() {
        return this == WALK || this == GROOMING || this == BIRTHDAY || this == ETC;
    }

    /**
     * 건강 관련 서브타입인지 확인
     */
    public boolean isHealthType() {
        return this == CHECKUP || this == TREATMENT || this == SURGERY;
    }
}
