package site.petful.healthservice.medical.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
import site.petful.healthservice.medical.dto.MedicationRequestDTO;
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

    /**
     * 복용약/영양제 일정 생성 (캘린더 기반)
     */
    @PostMapping("/medications")
    public ResponseEntity<ApiResponse<Long>> createMedication(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @RequestBody MedicationRequestDTO request
    ) {
        try {
            Long effectiveUserNo = (userNo != null) ? userNo : 1L;

            int days = (request.getDurationDays() == null || request.getDurationDays() <= 0) ? 1 : request.getDurationDays();
            String freq = (request.getFrequency() == null || request.getFrequency().isBlank())
                    ? (request.getAdministration() == null ? "하루 1회" : request.getAdministration())
                    : request.getFrequency();

            PrescriptionParsedDTO dto = new PrescriptionParsedDTO();
            PrescriptionParsedDTO.MedicationInfo info = new PrescriptionParsedDTO.MedicationInfo();
            info.setDrugName(request.getMedicationName());
            info.setDosage(request.getDosage());
            info.setAdministration(request.getAdministration());
            info.setFrequency(freq);
            info.setPrescriptionDays(days + "일");
            dto.setMedications(java.util.List.of(info));

            java.time.LocalDate base = (request.getStartDate() == null)
                    ? java.time.LocalDate.now() : request.getStartDate();

            java.util.List<Calendar> saved = medicationScheduleService
                    .registerMedicationSchedules(dto, effectiveUserNo, base);

            Long calNo = saved.isEmpty() ? null : saved.get(0).getCalNo();
            return ResponseEntity.ok(ApiResponseGenerator.success(calNo));
        } catch (BusinessException e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(ErrorCode.OPERATION_FAILED, "복용약 등록 실패"));
        }
    }

    

    

}
