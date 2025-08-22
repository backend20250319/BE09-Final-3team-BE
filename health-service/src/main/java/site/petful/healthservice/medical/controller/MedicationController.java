package site.petful.healthservice.medical.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;
import site.petful.healthservice.common.response.ErrorCode;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.medical.service.MedicationService;
import site.petful.healthservice.medical.dto.PrescriptionParsedDTO;

import java.io.IOException;

@RestController
@RequestMapping("/medical")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    
    /**
     * Health Service 상태를 확인합니다.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponseGenerator.success("Health Service is running"));
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

    /**
     * 처방전 이미지를 업로드하여 OCR로 분석하고 파싱된 정보를 반환합니다.
     */
    @PostMapping("/ocr/prescriptions/upload")
    public ResponseEntity<ApiResponse<PrescriptionParsedDTO>> uploadPrescription(@RequestParam("file") MultipartFile file) {
        try {
            PrescriptionParsedDTO result = medicationService.processPrescription(file);
            return ResponseEntity.ok(ApiResponseGenerator.success(result));
        } catch (BusinessException e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(ErrorCode.NETWORK_ERROR, "처방전 처리 실패"));
        }
    }

    /**
     * 이미 OCR 응답이 있는 경우 파싱만 수행합니다.
     */
    @PostMapping("/ocr/prescriptions/parse")
    public ResponseEntity<ApiResponse<PrescriptionParsedDTO>> parsePrescription(@RequestBody String ocrResponseJson) {
        try {
            PrescriptionParsedDTO result = medicationService.parsePrescription(ocrResponseJson);
            return ResponseEntity.ok(ApiResponseGenerator.success(result));
        } catch (BusinessException e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(ErrorCode.NETWORK_ERROR, "처방전 파싱 실패"));
        }
    }

}
