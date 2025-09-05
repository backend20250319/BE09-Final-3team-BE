package site.petful.communityservice.common;

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

    //아래 는 2000~8000번대 까지는 비즈니스 로직 오류 코드 알아서 정의해서 사용 아래는 예시
    // 2000번대 : 회원
    INSUFFICIENT_BALANCE("2000", "잔액이 부족합니다."),
    DUPLICATE_RESOURCE("2001", "이미 존재하는 리소스입니다."),
    OPERATION_FAILED("2002", "요청 처리에 실패했습니다."),

    // 3000번대 : 체험단

    // 4000번대 : 관리자

    // 5000번대 : 커뮤니티, 알람
    NOTIFICATION_NOT_FOUND("5000","알림이 존재하지 않습니다."),
    NOTIFICATION_FORBIDDEN("5001","해당 알림에 접근할 수 없습니다."),
    NOTIFICATION_SEND_FAILED("5002","알림 전송에 실패했습니다."),
    POST_NOT_FOUND("5003","게시글을 찾을 수 없습니다."),
    POST_FORBIDDEN("5004","해당 게시글에 접근할 수 없습니다."),
    POST_CREATE_FAILED("5005","게시글 생성에 실패했습니다."),
    POST_UPDATE_FAILED("5006","게시글 수정에 실패했습니다."),
    POST_DELETE_FAILED("5007","게시글 삭제에 실패했습니다."),
    COMMENT_NOT_FOUND("5008","댓글을 찾을 수 없습니다."),
    COMMENT_FORBIDDEN("5009","해당 댓글에 접근할 수 없습니다."),
    COMMENT_CREATE_FAILED("5010","댓글 생성에 실패했습니다."),
    COMMENT_UPDATE_FAILED("5011","댓글 수정에 실패했습니다."),
    COMMENT_DELETE_FAILED("5012","댓글 삭제에 실패했습니다."),
    INVALID_POST_ID("5013","유효하지 않은 게시글 ID입니다."),
    INVALID_COMMENT_ID("5014","유효하지 않은 댓글 ID입니다."),
    INVALID_USER_ID("5015","유효하지 않은 사용자 ID입니다."),
    INVALID_CONTENT("5016","유효하지 않은 내용입니다."),
    // 6000번대 : 건강관리

    // 7000번대 : SNS 관리

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