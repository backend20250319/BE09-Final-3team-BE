package site.petful.campaignservice.common;

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

    // 3000번대 : 회원

    // 4000번대 : 광고주, 체험단
    ADVERTISER_NOT_FOUND("4000", "해당 광고주는 존재하지 않습니다."),
    AD_NOT_FOUND("4001", "해당 광고는 존재하지 않습니다."),
    AD_INVALID_REQUEST("4002", "잘못된 요청입니다."),
    AD_INTERNAL_SERVER_ERROR("4003", "서버 내부 오류가 발생했습니다."),
    AD_NOT_MATCHED("4004", "해당 조건에 맞는 광고가 없습니다."),
    APPLICANT_NOT_FOUND("4005", "해당 지원자는 존재하지 않습니다."),


    // 5000번대 : 관리자

    // 6000번대 : 커뮤니티, 알람

    // 7000번대 : 건강관리

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
