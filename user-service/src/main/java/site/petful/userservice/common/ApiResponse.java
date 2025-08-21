package site.petful.userservice.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ApiResponse<T> {

    private String code;

    private String message;

    private String messageCode; // 추후 message.properties 와 활용도 가능

    private T data;

    ApiResponse(ErrorCode errorCode) {
        this(errorCode, null, null);
    }

    ApiResponse(ErrorCode errorCode, T data) {
        this.code = errorCode.getCode();
        this.message = errorCode.getDefaultMessage();
        this.data = data;
    }

    ApiResponse(ErrorCode errorCode, String responseMessage, T data) {
        this.code = errorCode.getCode();
        this.message = (responseMessage == null ? errorCode.getDefaultMessage() : responseMessage);
        this.data = data;
    }

    ApiResponse(ErrorCode errorCode, String message, String messageCode, T data) {
        this.code = errorCode.getCode();
        this.message = message;
        this.messageCode = messageCode;
        this.data = data;
    }
}