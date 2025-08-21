package site.petful.healthservice.medical.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;
import site.petful.healthservice.common.response.ErrorCode;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.medical.service.MedicationService;

import java.io.IOException;

@RestController
@RequestMapping("/medical")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadMedication(@RequestParam("file") MultipartFile file) {
        try {
            String result = medicationService.uploadMedicationImage(file);
            return ResponseEntity.ok(ApiResponseGenerator.success(result));
        } catch (BusinessException e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(e.getErrorCode(), e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(ErrorCode.OPERATION_FAILED, "파일 저장 실패"));
        }
    }

    @PostMapping("/ocr")
    public ResponseEntity<ApiResponse<String>> extractText(@RequestParam("file") MultipartFile file) {
        try {
            String result = medicationService.processOcrImage(file);
            return ResponseEntity.ok(ApiResponseGenerator.success(result));
        } catch (BusinessException e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(ErrorCode.NETWORK_ERROR, "OCR 처리 실패"));
        }
    }
}
