package site.petful.healthservice.medical.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;
import site.petful.healthservice.common.response.ErrorCode;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.medical.service.MedicationService;
import site.petful.healthservice.medical.service.MedicationScheduleService;
import site.petful.healthservice.medical.dto.PrescriptionParsedDTO;
import site.petful.healthservice.common.enums.CalendarSubType;
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
    }

    @PostMapping("/ocr")
    public ResponseEntity<ApiResponse<PrescriptionParsedDTO>> extractText(@RequestParam("file") MultipartFile file) {
        try {
            PrescriptionParsedDTO result = medicationService.processPrescription(file);
            List<Calendar> saved = medicationScheduleService.registerMedicationSchedules(result, 1L, LocalDate.now());
            return ResponseEntity.ok(ApiResponseGenerator.success(result));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OCR_PROCESSING_FAILED, e);
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
            CalendarSubType subTypeEnum =
                    (request.getSubType() != null && request.getSubType().equalsIgnoreCase("SUPPLEMENT"))
                            ? CalendarSubType.SUPPLEMENT
                            : CalendarSubType.PILL;

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
    }

    /**
     * 복용약/영양제 일정 수정 (부분 업데이트 허용)
     */
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<Long>> updateMedication(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @RequestParam("calNo") Long calNo,
            @RequestBody MedicationRequestDTO request
    ) {
            Long effectiveUserNo = (userNo != null) ? userNo : 1L;

            // 조회 및 소유자 검증
            java.util.Optional<Calendar> opt = medicationScheduleService.getCalendarRepository().findById(calNo);
            if (opt.isEmpty()) {
                throw new BusinessException(ErrorCode.MEDICATION_NOT_FOUND, "일정을 찾을 수 없습니다.");
            }
            Calendar entity = opt.get();
            if (!entity.getUserNo().equals(effectiveUserNo)) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
            }

            // 삭제된 일정 수정 방지
            if (Boolean.TRUE.equals(entity.getDeleted())) {
                throw new BusinessException(ErrorCode.MEDICATION_VALIDATION_FAILED, "삭제된 일정입니다.");
            }

            // 부분 업데이트: 요청에 있는 값만 반영
            String title = request.getMedicationName() != null || request.getDosage() != null
                    ? (request.getMedicationName() == null ? entity.getMedicationName() : request.getMedicationName()) +
                      (request.getDosage() == null ? (entity.getDosage() == null ? "" : " " + entity.getDosage()) : " " + request.getDosage())
                    : entity.getTitle();

            java.time.LocalDate base = request.getStartDate() != null ? request.getStartDate() : entity.getStartDate().toLocalDate();
            Integer duration = request.getDurationDays() != null ? request.getDurationDays() : entity.getDurationDays();
            String admin = request.getAdministration() != null ? request.getAdministration() : entity.getInstructions();
            String freq = request.getFrequency() != null ? request.getFrequency() : entity.getFrequency();

            // 빈도/시간 재계산
            var freqInfo = medicationScheduleService.parseFrequencyPublic(freq);
            java.util.List<java.time.LocalTime> slots = medicationScheduleService.getDefaultSlotsPublic(freqInfo.getTimesPerDay());
            if (admin != null && admin.contains("식후")) slots = medicationScheduleService.addMinutesPublic(slots, 30);

            java.time.LocalDateTime startDt = java.time.LocalDateTime.of(base, slots.get(0));
            java.time.LocalDate endDay = base.plusDays(Math.max(0, duration - 1));
            java.time.LocalDateTime endDt = java.time.LocalDateTime.of(endDay, slots.get(slots.size() - 1));

            entity.updateSchedule(title, startDt, endDt, admin, startDt);
            entity.updateMedicationInfo(
                    request.getMedicationName() != null ? request.getMedicationName() : entity.getMedicationName(),
                    request.getDosage() != null ? request.getDosage() : entity.getDosage(),
                    freq,
                    duration,
                    admin
            );
            entity.updateRecurrence(freqInfo.getRecurrenceType(), freqInfo.getInterval(), endDt);

            // 서브타입 변경
            if (request.getSubType() != null) {
                CalendarSubType sub = request.getSubType().equalsIgnoreCase("SUPPLEMENT") ? CalendarSubType.SUPPLEMENT : CalendarSubType.PILL;
                entity.updateSubType(sub);
            }

            // 알림 변경
            if (request.getReminderDaysBefore() != null) {
                entity.updateReminders(java.util.List.of(request.getReminderDaysBefore()));
            }

            medicationScheduleService.getCalendarRepository().save(entity);
            return ResponseEntity.ok(ApiResponseGenerator.success(entity.getCalNo()));
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

            java.util.Optional<Calendar> opt = medicationScheduleService.getCalendarRepository().findById(calNo);
            if (opt.isEmpty()) {
                throw new BusinessException(ErrorCode.MEDICATION_NOT_FOUND, "일정을 찾을 수 없습니다.");
            }
            Calendar entity = opt.get();
            // deleted 값 null 방지
            entity.ensureDeletedFlag();
            if (!entity.getUserNo().equals(effectiveUserNo)) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
            }

            // 이미 삭제된 경우 예외
            if (Boolean.TRUE.equals(entity.getDeleted())) {
                throw new BusinessException(ErrorCode.MEDICATION_VALIDATION_FAILED, "이미 삭제된 일정입니다.");
            }

            entity.softDelete();
            medicationScheduleService.getCalendarRepository().save(entity);
            return ResponseEntity.ok(ApiResponseGenerator.success(calNo));
    }

    

    

}
