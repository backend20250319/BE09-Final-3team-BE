package site.petful.healthservice.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;
import site.petful.healthservice.common.response.ErrorCode;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.converter.HttpMessageNotReadableException;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {



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
                .map(ObjectError::getDefaultMessage)
                .orElse("요청 데이터가 올바르지 않습니다.");
        
        String requestURI = e.getNestedPath();
        if (requestURI != null && requestURI.contains("/care")) {
            return ResponseEntity.ok(ApiResponseGenerator.failWithData(ErrorCode.MEDICAL_SCHEDULE_CREATION_FAILED, msg));
        } else if (requestURI != null && requestURI.contains("/medication")) {
            return ResponseEntity.ok(ApiResponseGenerator.failWithData(ErrorCode.MEDICATION_VALIDATION_FAILED, msg));
        } else {
            return ResponseEntity.ok(ApiResponseGenerator.failWithData(ErrorCode.INVALID_REQUEST, msg));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception e) {
        log.error("Unexpected exception occurred", e);
        return ResponseEntity.ok(ApiResponseGenerator.fail(ErrorCode.SYSTEM_ERROR));
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidFormat(InvalidFormatException e) {
        log.warn("Invalid format: {}", e.getOriginalMessage());
        return ResponseEntity.ok(ApiResponseGenerator.fail(ErrorCode.MEDICAL_DATE_FORMAT_ERROR));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleNotReadable(HttpMessageNotReadableException e) {
        Throwable root = e.getMostSpecificCause();
        if (root instanceof java.time.format.DateTimeParseException || root instanceof InvalidFormatException) {
            log.warn("Date parse error: {}", root.getMessage());
            return ResponseEntity.ok(ApiResponseGenerator.fail(ErrorCode.MEDICAL_DATE_FORMAT_ERROR));
        }
        log.warn("Invalid request body: {}", e.getMessage());
        return ResponseEntity.ok(ApiResponseGenerator.fail(ErrorCode.INVALID_REQUEST));
    }
}
