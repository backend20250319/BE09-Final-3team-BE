package site.petful.healthservice.medical.medication.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;
import site.petful.healthservice.medical.medication.dto.*;
import site.petful.healthservice.medical.medication.service.MedicationScheduleService;
import site.petful.healthservice.medical.medication.service.MedicationService;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.time.LocalDate;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.common.response.ErrorCode;
import site.petful.healthservice.schedule.entity.Schedule;

@RestController
@RequestMapping("/medical")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationScheduleService medicationScheduleService;
    private final MedicationService medicationService;

    
    /**
     * 복용약/영양제 일정 생성 (캘린더 기반)
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Long>> createMedication(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @Valid @RequestBody MedicationRequestDTO request
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        Long calNo = medicationScheduleService.createMedication(effectiveUserNo, request);
        return ResponseEntity.ok(ApiResponseGenerator.success(calNo));
    }


    /**
     * 복용약/영양제 일정 목록 조회
     */
    @GetMapping("/read")
    public ResponseEntity<ApiResponse<List<MedicationResponseDTO>>> listMedications(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "subtype", required = false) String subType
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        List<MedicationResponseDTO> result = medicationScheduleService.listMedications(effectiveUserNo, from, to, subType);
        return ResponseEntity.ok(ApiResponseGenerator.success(result));
    }

    /**
     * 복용약/영양제 일정 상세 조회
     */
    @GetMapping("/{calNo}")
    public ResponseEntity<ApiResponse<MedicationDetailDTO>> getMedicationDetail(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @PathVariable("calNo") Long calNo
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        MedicationDetailDTO dto = medicationScheduleService.getMedicationDetail(calNo, effectiveUserNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(dto));
    }


    /**
     * 복용약/영양제 일정 수정 (부분 업데이트 허용)
     */
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<MedicationUpdateDiffDTO>> updateMedication(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @RequestParam("calNo") Long calNo,
            @RequestBody MedicationUpdateRequestDTO request
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        MedicationUpdateDiffDTO response = medicationScheduleService.updateMedication(calNo, request, effectiveUserNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }


    /**
     * 알림 on/off 전용 API 
     */
    @PatchMapping("/alarm")
    public ResponseEntity<ApiResponse<Long>> toggleAlarm(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @RequestParam("calNo") Long calNo,
            @RequestParam("enabled") boolean enabled
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        Long updatedCalNo = medicationScheduleService.toggleAlarm(calNo, effectiveUserNo, enabled);
        return ResponseEntity.ok(ApiResponseGenerator.success(updatedCalNo));
    }


    /**
     * 복용약/영양제 일정 삭제
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Long>> deleteMedication(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @RequestParam("calNo") Long calNo
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        Long deletedCalNo = medicationScheduleService.deleteMedication(calNo, effectiveUserNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(deletedCalNo));
    }


    /**
     * 투약 관련 메타 정보 조회 (드롭다운용)
     */
    @GetMapping("/meta")
    public ResponseEntity<ApiResponse<java.util.Map<String, java.util.List<String>>>> getMedicationMeta() {
        java.util.Map<String, java.util.List<String>> data = medicationScheduleService.getMedicationMeta();
        return ResponseEntity.ok(ApiResponseGenerator.success(data));
    }

    /**
     * 처방전 OCR 처리 및 일정 자동 등록
     */
    @PostMapping("/ocr")
    public ResponseEntity<ApiResponse<PrescriptionParsedDTO>> extractText(@RequestParam("file") MultipartFile file) {
        try {
            PrescriptionParsedDTO result = medicationService.processPrescription(file);
            List<Schedule> saved = medicationScheduleService.registerMedicationSchedules(result, 1L, LocalDate.now());
            return ResponseEntity.ok(ApiResponseGenerator.success(result));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OCR_PROCESSING_FAILED, e);
        }
    }
}
