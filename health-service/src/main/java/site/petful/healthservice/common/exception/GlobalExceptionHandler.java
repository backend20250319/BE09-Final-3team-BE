package site.petful.healthservice.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;
import site.petful.healthservice.common.response.ErrorCode;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception occurred: {}", e.getMessage());
        return ResponseEntity.ok(ApiResponseGenerator.fail(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("요청 데이터가 올바르지 않습니다.");
        return ResponseEntity.ok(ApiResponseGenerator.fail(ErrorCode.MEDICATION_VALIDATION_FAILED, msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("Unexpected exception occurred", e);
        return ResponseEntity.ok(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR));
    }
}
