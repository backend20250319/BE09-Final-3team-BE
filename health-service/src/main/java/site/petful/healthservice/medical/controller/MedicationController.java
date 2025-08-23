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
import site.petful.healthservice.medical.dto.MedicationResponseDTO;
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

    /**
     * 복용약/영양제 일정 조회 (기간/서브타입 필터)
     */
    @GetMapping("/read")
    public ResponseEntity<ApiResponse<java.util.List<MedicationResponseDTO>>> listMedications(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "subType", required = false) String subType
    ) {
        try {
            Long effectiveUserNo = (userNo != null) ? userNo : 1L;
            java.time.LocalDateTime start = (from == null || from.isBlank())
                    ? java.time.LocalDate.now().minusMonths(1).atStartOfDay()
                    : java.time.LocalDate.parse(from).atStartOfDay();
            java.time.LocalDateTime end = (to == null || to.isBlank())
                    ? java.time.LocalDate.now().plusMonths(1).atTime(23,59,59)
                    : java.time.LocalDate.parse(to).atTime(23,59,59);

            // 캘린더에서 사용자/기간으로 조회 후, MEDICATION만 필터
            java.util.List<Calendar> items = medicationScheduleService
                    .getCalendarRepository()
                    .findByUserNoAndDateRange(effectiveUserNo, start, end);

            java.util.stream.Stream<Calendar> stream = items.stream()
                    .filter(c -> c.getMainType() == site.petful.healthservice.common.enums.CalendarMainType.MEDICATION);
            if (subType != null && !subType.isBlank()) {
                stream = stream.filter(c -> c.getSubType().name().equalsIgnoreCase(subType));
            }

            java.util.List<MedicationResponseDTO> result = stream
                    .map(c -> MedicationResponseDTO.builder()
                            .calNo(c.getCalNo())
                            .title(c.getTitle())
                            .startDate(c.getStartDate())
                            .endDate(c.getEndDate())
                            .alarmTime(c.getAlarmTime())
                            .mainType(c.getMainType().name())
                            .subType(c.getSubType().name())
                            .medicationName(c.getMedicationName())
                            .dosage(c.getDosage())
                            .frequency(c.getFrequency())
                            .durationDays(c.getDurationDays())
                            .instructions(c.getInstructions())
                            .build())
                    .toList();

            return ResponseEntity.ok(ApiResponseGenerator.success(result));
        } catch (BusinessException e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(ErrorCode.OPERATION_FAILED, "복용약 조회 실패"));
        }
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
    @PostMapping("/create")
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

            // 서브타입 매핑
            site.petful.healthservice.common.enums.CalendarSubType subTypeEnum =
                    (request.getSubType() != null && request.getSubType().equalsIgnoreCase("SUPPLEMENT"))
                            ? site.petful.healthservice.common.enums.CalendarSubType.SUPPLEMENT
                            : site.petful.healthservice.common.enums.CalendarSubType.PILL;

            java.util.List<Calendar> saved = medicationScheduleService
                    .registerMedicationSchedules(dto, effectiveUserNo, base, subTypeEnum);

            // 알림 시기 반영: reminderDaysBefore (당일=0, 1/2/3일 전)
            if (request.getReminderDaysBefore() != null && !saved.isEmpty()) {
                java.util.List<Integer> reminders = java.util.List.of(request.getReminderDaysBefore());
                for (Calendar c : saved) {
                    c.updateReminders(reminders);
                }
            }

            Long calNo = saved.isEmpty() ? null : saved.get(0).getCalNo();
            return ResponseEntity.ok(ApiResponseGenerator.success(calNo));
        } catch (BusinessException e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(e.getErrorCode(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponseGenerator.failGeneric(ErrorCode.OPERATION_FAILED, "복용약 등록 실패"));
        }
    }

    

    

}
