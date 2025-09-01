package site.petful.healthservice.medical.medication.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;
import site.petful.healthservice.medical.medication.dto.*;
import site.petful.healthservice.medical.medication.service.MedicationScheduleService;
import site.petful.healthservice.medical.medication.service.MedicationService;

import java.io.IOException;
import java.util.List;
import java.time.LocalDate;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.common.response.ErrorCode;
import site.petful.healthservice.medical.schedule.entity.Schedule;

@RestController
@RequestMapping("/medical/medication")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationScheduleService medicationScheduleService;
    private final MedicationService medicationService;

    
    /**
     * 복용약/영양제 일정 생성 (캘린더 기반)
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Long>> createMedication(
            @AuthenticationPrincipal String userNo,
            @Valid @RequestBody MedicationRequestDTO request
    ) {
        Long calNo = medicationScheduleService.createMedication(Long.valueOf(userNo), request);
        return ResponseEntity.ok(ApiResponseGenerator.success(calNo));
    }


    /**
     * 복용약/영양제 일정 목록 조회
     */
    @GetMapping("/read")
    public ResponseEntity<ApiResponse<List<MedicationResponseDTO>>> listMedications(
            @AuthenticationPrincipal String userNo,
            @RequestParam(value = "petNo") Long petNo,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "subtype", required = false) String subType
    ) {
        List<MedicationResponseDTO> result = medicationScheduleService.listMedications(Long.valueOf(userNo), petNo, from, to, subType);
        return ResponseEntity.ok(ApiResponseGenerator.success(result));
    }

    /**
     * 복용약/영양제 일정 상세 조회
     */
    @GetMapping("/{calNo}")
    public ResponseEntity<ApiResponse<MedicationDetailDTO>> getMedicationDetail(
            @AuthenticationPrincipal String userNo,
            @PathVariable("calNo") Long calNo
    ) {
        MedicationDetailDTO dto = medicationScheduleService.getMedicationDetail(calNo, Long.valueOf(userNo));
        return ResponseEntity.ok(ApiResponseGenerator.success(dto));
    }


    /**
     * 복용약/영양제 일정 수정 (부분 업데이트 허용)
     */
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<MedicationUpdateDiffDTO>> updateMedication(
            @AuthenticationPrincipal String userNo,
            @RequestParam("calNo") Long calNo,
            @RequestBody MedicationUpdateRequestDTO request
    ) {
        MedicationUpdateDiffDTO response = medicationScheduleService.updateMedication(calNo, request, Long.valueOf(userNo));
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }


    /**
     * 알림 on/off 전용 API 
     */
    @PatchMapping("/alarm")
    public ResponseEntity<ApiResponse<Boolean>> toggleAlarm(
            @AuthenticationPrincipal String userNo,
            @RequestParam("calNo") Long calNo
    ) {
        Boolean alarmEnabled = medicationScheduleService.toggleAlarm(calNo, Long.valueOf(userNo));
        return ResponseEntity.ok(ApiResponseGenerator.success(alarmEnabled));
    }


    /**
     * 복용약/영양제 일정 삭제
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Long>> deleteMedication(
            @AuthenticationPrincipal String userNo,
            @RequestParam("calNo") Long calNo
    ) {
        Long deletedCalNo = medicationScheduleService.deleteMedication(calNo, Long.valueOf(userNo));
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
    public ResponseEntity<ApiResponse<PrescriptionParsedDTO>> extractText(
            @AuthenticationPrincipal String userNo,
            @RequestParam("file") MultipartFile file,
            @RequestParam("petNo") Long petNo
    ) {
        PrescriptionParsedDTO result = medicationService.processPrescription(file);
        List<Schedule> saved = medicationScheduleService.registerMedicationSchedules(result, Long.valueOf(userNo), petNo, LocalDate.now());
        return ResponseEntity.ok(ApiResponseGenerator.success(result));
    }
}
