package site.petful.healthservice.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;
import site.petful.healthservice.common.response.ErrorCode;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import site.petful.healthservice.exception.AuthenticationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication exception occurred: {}", e.getMessage());
        return ResponseEntity.ok(ApiResponseGenerator.fail(ErrorCode.UNAUTHORIZED, e.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e) {
        log.warn("Business exception occurred: {}", e.getMessage());
        // 커스텀 메시지를 그대로 전달
        return ResponseEntity.ok(ApiResponseGenerator.fail(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("요청 데이터가 올바르지 않습니다.");
        return ResponseEntity.ok(ApiResponseGenerator.failWithData(ErrorCode.MEDICATION_VALIDATION_FAILED, msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception e) {
        log.error("Unexpected exception occurred", e);
        return ResponseEntity.ok(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR));
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidFormat(InvalidFormatException e) {
        log.warn("Invalid format: {}", e.getOriginalMessage());
        return ResponseEntity.ok(ApiResponseGenerator.fail(ErrorCode.INVALID_DATE_FORMAT));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleNotReadable(HttpMessageNotReadableException e) {
        Throwable root = e.getMostSpecificCause() != null ? e.getMostSpecificCause() : e.getCause();
        if (root instanceof java.time.format.DateTimeParseException || root instanceof InvalidFormatException) {
            log.warn("Date parse error: {}", root.getMessage());
            return ResponseEntity.ok(ApiResponseGenerator.fail(ErrorCode.INVALID_DATE_FORMAT));
        }
        log.warn("Invalid request body: {}", e.getMessage());
        return ResponseEntity.ok(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
    }
}
