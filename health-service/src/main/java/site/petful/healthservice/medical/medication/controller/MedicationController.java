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
import site.petful.healthservice.medical.medication.service.MedicationOCRService;
import site.petful.healthservice.medical.medication.repository.ScheduleMedicationDetailRepository;
import site.petful.healthservice.medical.medication.entity.ScheduleMedDetail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.common.response.ErrorCode;
import site.petful.healthservice.medical.schedule.entity.Schedule;

@Slf4j
@RestController
@RequestMapping("/medical/medication")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationScheduleService medicationScheduleService;
    private final MedicationOCRService medicationOCRService;
    private final ScheduleMedicationDetailRepository medicationDetailRepository;

    
    /**
     * 복용약/영양제 일정 생성 (캘린더 기반)
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Long>> createMedication(
            @AuthenticationPrincipal String userNo,
            @Valid @RequestBody MedicationRequestDTO request
    ) {
        log.info("=== 투약 일정 생성 API 호출 ===");
        log.info("userNo: {}", userNo);
        log.info("request: {}", request);
        log.info("medicationFrequency: {}", request.getMedicationFrequency());
        log.info("times: {}", request.getTimes());
        log.info("petNo: {}", request.getPetNo());
        log.info("name: {}", request.getName());
        log.info("startDate: {}", request.getStartDate());
        log.info("endDate: {}", request.getEndDate());
        
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
            @RequestParam(value = "subtype", required = false) String subType,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "isPrescription", required = false) Boolean isPrescription
    ) {
        List<MedicationResponseDTO> result = medicationScheduleService.listMedications(Long.valueOf(userNo), petNo, from, to, subType, type, isPrescription);
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
            @RequestParam(defaultValue = "true") boolean isEditMode,
            @RequestBody MedicationUpdateRequestDTO request
    ) {
        log.info("=== 투약 일정 수정 API 호출 ===");
        log.info("userNo: {}", userNo);
        log.info("calNo: {}", calNo);
        log.info("isEditMode: {}", isEditMode);
        log.info("request: {}", request);
        log.info("request.getFrequency(): {}", request.getFrequency());
        
        MedicationUpdateDiffDTO response = medicationScheduleService.updateMedication(calNo, request, Long.valueOf(userNo), isEditMode);
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
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getMedicationMeta() {
        Map<String, List<String>> data = medicationScheduleService.getMedicationMeta();
        return ResponseEntity.ok(ApiResponseGenerator.success(data));
    }

    /**
     * 처방전 OCR 처리 및 일정 자동 등록
     */
    @PostMapping(value = "/ocr", produces = "application/json", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> extractText(
            @AuthenticationPrincipal String userNo,
            @RequestParam("file") MultipartFile file,
            @RequestParam("petNo") Long petNo
    ) {
        log.info("=== OCR 요청 수신 ===");
        log.info("userNo: {}", userNo);
        log.info("petNo: {}", petNo);
        log.info("file: {}", file);
        log.info("file.isEmpty(): {}", file.isEmpty());
        log.info("file.getOriginalFilename(): {}", file.getOriginalFilename());
        // 파일 검증
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "파일이 선택되지 않았습니다.");
        }
        
        try {
            // OCR 처리
            PrescriptionParsedDTO result = medicationOCRService.processPrescription(file);
            log.info("=== OCR 처리 완료 ===");
            log.info("파싱된 약물 수: {}", result.getMedications() != null ? result.getMedications().size() : 0);
            
            // 일정 등록
            List<Schedule> saved = medicationScheduleService.registerMedicationSchedules(result, Long.valueOf(userNo), petNo, LocalDate.now());
            log.info("=== 일정 등록 완료 ===");
            log.info("생성된 일정 수: {}", saved.size());
            
            // 생성된 일정의 isPrescription 확인
            for (Schedule schedule : saved) {
                log.info("일정 ID: {}, 제목: {}", schedule.getScheduleNo(), schedule.getTitle());
            }
            
            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("createdSchedules", saved.size());
            response.put("message", "처방전이 성공적으로 등록되었습니다.");
            
            // 생성된 일정 ID 목록
            List<Long> scheduleNo = saved.stream()
                    .map(Schedule::getScheduleNo)
                    .collect(java.util.stream.Collectors.toList());
            response.put("scheduleNo", scheduleNo);
            
            // 상세한 약물 정보 추가
            List<Map<String, Object>> medications = new ArrayList<>();
            for (Schedule schedule : saved) {
                Map<String, Object> medication = new HashMap<>();
                medication.put("id", schedule.getScheduleNo());
                medication.put("name", schedule.getTitle());
                medication.put("type", schedule.getSubType().name());
                medication.put("frequency", schedule.getFrequency());
                medication.put("startDate", schedule.getStartDate().toLocalDate().toString());
                medication.put("endDate", schedule.getEndDate() != null ? 
                    schedule.getEndDate().toLocalDate().toString() : null);
                
                // 상세 정보 추가 (medicationDetails에서)
                try {
                    Optional<ScheduleMedDetail> detailOpt = medicationDetailRepository.findById(schedule.getScheduleNo());
                    if (detailOpt.isPresent()) {
                        ScheduleMedDetail detail = detailOpt.get();
                        medication.put("dosage", detail.getDosage());
                        medication.put("duration", detail.getDurationDays() != null ? 
                            detail.getDurationDays() + "일간" : null);
                    }
                } catch (Exception e) {
                    log.warn("약물 상세 정보 조회 실패: {}", e.getMessage());
                }
                
                medications.add(medication);
            }
            response.put("medications", medications);
            
            // 최종 응답 로그
            log.info("=== 최종 응답 생성 ===");
            log.info("응답 데이터: {}", response);
            
            ApiResponse<Map<String, Object>> apiResponse = ApiResponseGenerator.success(response);
            log.info("API 응답 코드: {}", apiResponse.getCode());
            log.info("API 응답 메시지: {}", apiResponse.getMessage());
            
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            log.error("OCR 처리 중 오류 발생: ", e);
            log.error("오류 타입: {}", e.getClass().getSimpleName());
            log.error("오류 메시지: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("원인: {}", e.getCause().getMessage());
            }
            throw new BusinessException(ErrorCode.OCR_PROCESSING_FAILED, "OCR 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
