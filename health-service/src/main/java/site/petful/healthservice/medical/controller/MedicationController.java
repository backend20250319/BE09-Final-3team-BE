package site.petful.healthservice.medical.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
import site.petful.healthservice.medical.service.MedicationScheduleService;
import site.petful.healthservice.medical.dto.PrescriptionParsedDTO;
import site.petful.healthservice.common.entity.Calendar;
import java.time.LocalDate;
import java.util.List;

 

@RestController
@RequestMapping("/medical")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;
    private final MedicationScheduleService medicationScheduleService;

    
    /**
     * Health Service 상태를 확인합니다.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponseGenerator.success("Health Service is running"));
    }

    @PostMapping("/ocr")
    public ResponseEntity<ApiResponse<PrescriptionParsedDTO>> extractText(@RequestParam("file") MultipartFile file) {
        try {
            PrescriptionParsedDTO result = medicationService.processPrescription(file);
            // 일정 자동 등록: 임시로 userNo=1L, baseDate=오늘. FE에서 전달 받으면 교체
            List<Calendar> saved = medicationScheduleService.registerMedicationSchedules(result, 1L, LocalDate.now());
            return ResponseEntity.ok(ApiResponseGenerator.success(result));
        } catch (BusinessException e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(ErrorCode.NETWORK_ERROR, "OCR 처리 실패"));
        }
    }

    

    

}
