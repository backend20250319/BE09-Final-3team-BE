package site.petful.userservice.common;

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

    // 3000번대 : 사용자 관련 오류
    USER_NOT_FOUND("3000", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL("3001", "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD("3002", "비밀번호가 올바르지 않습니다."),
    EMAIL_VERIFICATION_REQUIRED("3003", "이메일 인증이 필요합니다."),
    EMAIL_VERIFICATION_FAILED("3004", "이메일 인증에 실패했습니다."),
    INVALID_TOKEN("3005", "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED("3006", "토큰이 만료되었습니다."),
    REFRESH_TOKEN_INVALID("3007", "리프레시 토큰이 유효하지 않습니다."),
    USER_ALREADY_VERIFIED("3008", "이미 인증된 사용자입니다."),
    INVALID_USER_TYPE("3009", "유효하지 않은 사용자 타입입니다."),
    USER_ACCOUNT_DISABLED("3010", "비활성화된 계정입니다."),
    SIGNUP_FAILED("3011", "회원가입에 실패했습니다."),
    LOGIN_FAILED("3012", "로그인에 실패했습니다."),
    PASSWORD_MISMATCH("3013", "비밀번호가 일치하지 않습니다."),
    EMAIL_SEND_FAILED("3014", "이메일 발송에 실패했습니다."),

    // 4000번대 : 체험단

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