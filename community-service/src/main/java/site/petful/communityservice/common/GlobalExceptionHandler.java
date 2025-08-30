package site.petful.communityservice.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public void handleBadBody(HttpMessageNotReadableException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청 본문이 올바르지 않습니다.");
    }
}
