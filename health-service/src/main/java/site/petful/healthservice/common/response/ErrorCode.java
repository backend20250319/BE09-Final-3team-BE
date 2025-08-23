package site.petful.healthservice.common.response;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // 2000번대 : 성공
    SUCCESS("2000", "OK"),

    // 1000번대 : 클라이언트 요청 오류
    INVALID_REQUEST("1000", "잘못된 요청입니다."),
    UNAUTHORIZED("1001", "권한이 없습니다."),
    FORBIDDEN("1002", "접근이 금지되었습니다."),
    NOT_FOUND("1003", "요청한 리소스를 찾을 수 없습니다."),

    //아래 는 3000~8000번대 까지는 비즈니스 로직 오류 코드 알아서 정의해서 사용 아래는 예시
    // 3000번대 : 회원
    INSUFFICIENT_BALANCE("3000", "잔액이 부족합니다."),
    DUPLICATE_RESOURCE("3001", "이미 존재하는 리소스입니다."),
    OPERATION_FAILED("3002", "요청 처리에 실패했습니다."),

    // 4000번대 : 체험단

    // 5000번대 : 관리자

    // 6000번대 : 커뮤니티, 알람

    // 7000번대 : 건강관리
    INVALID_MEDICATION_DATA("7000", "처방전 데이터가 유효하지 않습니다."),
    OCR_PARSING_FAILED("7001", "처방전 정보 추출에 실패했습니다."),
    SCHEDULE_CREATION_FAILED("7002", "일정 생성에 실패했습니다."),
    MEDICATION_NOT_FOUND("7003", "약물 정보를 찾을 수 없습니다."),
    INVALID_DOSAGE_FORMAT("7004", "용량 형식이 올바르지 않습니다."),
    INVALID_FREQUENCY_FORMAT("7005", "복용 빈도 형식이 올바르지 않습니다."),
    INVALID_PRESCRIPTION_DAYS("7006", "처방일수가 올바르지 않습니다."),
    OCR_PROCESSING_FAILED("7007", "OCR 처리 중 오류가 발생했습니다."),
    IMAGE_FORMAT_NOT_SUPPORTED("7008", "지원하지 않는 이미지 형식입니다."),
    IMAGE_SIZE_TOO_LARGE("7009", "이미지 크기가 너무 큽니다."),
    MEDICATION_SCHEDULE_CONFLICT("7010", "약물 일정이 중복됩니다."),
    INVALID_ADMINISTRATION_METHOD("7011", "복용법이 올바르지 않습니다."),
    MEDICATION_INTERACTION_WARNING("7012", "약물 상호작용 경고가 있습니다."),
    PRESCRIPTION_EXPIRED("7013", "처방전이 만료되었습니다."),
    INSUFFICIENT_MEDICATION_INFO("7014", "약물 정보가 부족합니다."),
    MEDICATION_ALREADY_REGISTERED("7015", "이미 등록된 약물입니다."),
    INVALID_MEDICATION_NAME("7016", "약물명이 올바르지 않습니다."),
    MEDICATION_DOSAGE_MISMATCH("7017", "용량 정보가 일치하지 않습니다."),
    MEDICATION_FREQUENCY_MISMATCH("7018", "복용 빈도 정보가 일치하지 않습니다."),
    MEDICATION_DURATION_MISMATCH("7019", "복용 기간 정보가 일치하지 않습니다."),
    MEDICATION_TEMPLATE_NOT_FOUND("7020", "처방전 템플릿을 찾을 수 없습니다."),
    MEDICATION_VALIDATION_FAILED("7021", "약물 정보 검증에 실패했습니다."),
    MEDICATION_PARSING_TIMEOUT("7022", "약물 정보 파싱 시간이 초과되었습니다."),
    MEDICATION_DATA_CORRUPTED("7023", "약물 데이터가 손상되었습니다."),
    MEDICATION_SERVICE_UNAVAILABLE("7024", "약물 서비스를 사용할 수 없습니다."),
    MEDICATION_QUOTA_EXCEEDED("7025", "약물 처리 할당량을 초과했습니다."),
    ALARM_ALREADY_ENABLED("7026", "알림이 이미 활성화되어 있습니다."),
    ALARM_ALREADY_DISABLED("7027", "알림이 이미 비활성화되어 있습니다."),

    // 8000번대 : SNS 관리

    // 9000번대 : 시스템 오류
    SYSTEM_ERROR("9000", "서버 내부 오류가 발생했습니다."),
    DATABASE_ERROR("9001", "데이터베이스 처리 중 오류가 발생했습니다."),
    NETWORK_ERROR("9002", "네트워크 오류가 발생했습니다."),
    UNKNOWN_ERROR("9999", "알수없는 오류가 발생했습니다.");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
