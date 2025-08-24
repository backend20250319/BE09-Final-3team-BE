package site.petful.healthservice.medical.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
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
import site.petful.healthservice.medical.repository.CalendarMedicationDetailRepository;
import site.petful.healthservice.medical.dto.PrescriptionParsedDTO;
import site.petful.healthservice.common.enums.CalendarSubType;
import site.petful.healthservice.common.enums.MedicationFrequency;
import site.petful.healthservice.common.entity.Calendar;
import site.petful.healthservice.medical.dto.MedicationRequestDTO;
import site.petful.healthservice.medical.dto.MedicationResponseDTO;
import site.petful.healthservice.medical.dto.MedicationUpdateDiffDTO;
import java.time.LocalDate;
import java.util.List;

 

@RestController
@RequestMapping("/medical")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;
    private final MedicationScheduleService medicationScheduleService;
    private final CalendarMedicationDetailRepository medicationDetailRepository;

    
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
    @GetMapping("/meta")
    public ResponseEntity<ApiResponse<java.util.Map<String, java.util.List<String>>>> medicationMeta() {
        java.util.List<String> subTypes = java.util.List.of("PILL","SUPPLEMENT");
        java.util.List<String> frequencies = java.util.Arrays.stream(MedicationFrequency.values())
                .map(Enum::name).toList();
        java.util.Map<String, java.util.List<String>> data = new java.util.HashMap<>();
        data.put("subTypes", subTypes);
        data.put("frequencies", frequencies);
        return ResponseEntity.ok(ApiResponseGenerator.success(data));
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
            java.time.LocalDateTime start;
            java.time.LocalDateTime end;
            try {
                start = (from == null || from.isBlank())
                        ? java.time.LocalDate.now().minusMonths(1).atStartOfDay()
                        : java.time.LocalDate.parse(from).atStartOfDay();
                end = (to == null || to.isBlank())
                        ? java.time.LocalDate.now().plusMonths(1).atTime(23,59,59)
                        : java.time.LocalDate.parse(to).atTime(23,59,59);
            } catch (java.time.format.DateTimeParseException e) {
                throw new BusinessException(ErrorCode.INVALID_DATE_FORMAT, "유효하지 않은 날짜 형식입니다.");
            }
            if (start.isAfter(end)) {
                throw new BusinessException(ErrorCode.INVALID_DATE_RANGE, "from이 to보다 늦을 수 없습니다.");
            }

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
                    .map(c -> medicationDetailRepository.findById(c.getCalNo()).map(detail ->
                            MedicationResponseDTO.builder()
                                    .calNo(c.getCalNo())
                                    .title(c.getTitle())
                                    .startDate(c.getStartDate())
                                    .endDate(c.getEndDate())
                                    .alarmTime(c.getAlarmTime())
                                    .mainType(c.getMainType().name())
                                    .subType(c.getSubType().name())
                                    .medicationName(detail.getMedicationName())
                                    .dosage(detail.getDosage())
                                    .frequency(c.getFrequency())
                                    .durationDays(detail.getDurationDays())
                                    .instructions(detail.getInstructions())
                                    .build()
                    ).orElseGet(() ->
                            MedicationResponseDTO.builder()
                                    .calNo(c.getCalNo())
                                    .title(c.getTitle())
                                    .startDate(c.getStartDate())
                                    .endDate(c.getEndDate())
                                    .alarmTime(c.getAlarmTime())
                                    .mainType(c.getMainType().name())
                                    .subType(c.getSubType().name())
                                    .medicationName(null)
                                    .dosage(null)
                                    .frequency(c.getFrequency())
                                    .durationDays(null)
                                    .instructions(null)
                                    .build()
                    ))
                    .toList();

            return ResponseEntity.ok(ApiResponseGenerator.success(result));
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

        var opt = medicationScheduleService.getCalendarRepository().findById(calNo);
        if (opt.isEmpty()) throw new BusinessException(ErrorCode.MEDICATION_NOT_FOUND, "일정을 찾을 수 없습니다.");

        Calendar entity = opt.get();
        if (!entity.getUserNo().equals(effectiveUserNo)) throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        if (Boolean.TRUE.equals(entity.getDeleted())) throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "삭제된 일정입니다.");

        boolean isOn = entity.getReminderDaysBefore() != null && !entity.getReminderDaysBefore().isEmpty();
        if (enabled) {
            if (isOn) throw new BusinessException(ErrorCode.ALARM_ALREADY_ENABLED);
            entity.updateReminders(java.util.List.of(0));
        } else {
            if (!isOn) throw new BusinessException(ErrorCode.ALARM_ALREADY_DISABLED);
            entity.updateReminders(java.util.List.of());
        }

        medicationScheduleService.getCalendarRepository().save(entity);
        return ResponseEntity.ok(ApiResponseGenerator.success(entity.getCalNo()));
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
            @Valid @RequestBody MedicationRequestDTO request
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
    public ResponseEntity<ApiResponse<MedicationUpdateDiffDTO>> updateMedication(
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
                throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "삭제된 일정입니다.");
            }

            // 변경 전 스냅샷 수집 (detail 우선)
            java.util.Optional<site.petful.healthservice.medical.entity.CalendarMedDetail> optDetail = medicationDetailRepository.findById(calNo);
            site.petful.healthservice.medical.entity.CalendarMedDetail beforeDetail = optDetail.orElse(null);
            Integer beforeReminder = (entity.getReminderDaysBefore() == null || entity.getReminderDaysBefore().isEmpty())
                    ? null : entity.getReminderDaysBefore().get(0);
            MedicationUpdateDiffDTO.Snapshot before = MedicationUpdateDiffDTO.Snapshot.builder()
                    .title(entity.getTitle())
                    .startDate(entity.getStartDate())
                    .endDate(entity.getEndDate())
                    .medicationName(beforeDetail != null ? beforeDetail.getMedicationName() : null)
                    .dosage(beforeDetail != null ? beforeDetail.getDosage() : null)
                    .frequency(entity.getFrequency())
                    .durationDays(beforeDetail != null ? beforeDetail.getDurationDays() : null)
                    .instructions(beforeDetail != null ? beforeDetail.getInstructions() : null)
                    .subType(entity.getSubType().name())
                    .reminderDaysBefore(beforeReminder)
                    .build();
            String title = request.getMedicationName() != null || request.getDosage() != null
                    ? (request.getMedicationName() == null ? entity.getTitle() : request.getMedicationName()) +
                      (request.getDosage() == null ? "" : " " + request.getDosage())
                    : entity.getTitle();

            java.time.LocalDate base = request.getStartDate() != null ? request.getStartDate() : entity.getStartDate().toLocalDate();
            Integer duration = request.getDurationDays() != null ? request.getDurationDays() : null;
            String admin = request.getAdministration() != null ? request.getAdministration() : null;
            String freq = request.getFrequency() != null ? request.getFrequency() : entity.getFrequency();

            // 빈도/시간 재계산
            var freqInfo = medicationScheduleService.parseFrequencyPublic(freq);
            java.util.List<java.time.LocalTime> slots = medicationScheduleService.getDefaultSlotsPublic(freqInfo.getTimesPerDay());
            if (admin != null && admin.contains("식후")) slots = medicationScheduleService.addMinutesPublic(slots, 30);

            java.time.LocalDateTime startDt = java.time.LocalDateTime.of(base, slots.get(0));
            java.time.LocalDate endDay = base.plusDays(Math.max(0, (duration == null ? 1 : duration) - 1));
            java.time.LocalDateTime endDt = java.time.LocalDateTime.of(endDay, slots.get(slots.size() - 1));

            entity.updateSchedule(title, startDt, endDt, startDt);
            entity.updateFrequency(freq);
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

            // detail 동기화(존재할 때만)
            optDetail.ifPresent(d -> {
                if (request.getMedicationName() != null) d.setMedicationName(request.getMedicationName());
                if (request.getDosage() != null) d.setDosage(request.getDosage());
                if (request.getDurationDays() != null) d.setDurationDays(request.getDurationDays());
                if (request.getAdministration() != null) d.setInstructions(request.getAdministration());
                medicationDetailRepository.save(d);
            });

            Integer afterReminder = (entity.getReminderDaysBefore() == null || entity.getReminderDaysBefore().isEmpty())
                    ? null : entity.getReminderDaysBefore().get(0);
            site.petful.healthservice.medical.entity.CalendarMedDetail afterDetail = medicationDetailRepository.findById(calNo).orElse(beforeDetail);
            MedicationUpdateDiffDTO.Snapshot after = MedicationUpdateDiffDTO.Snapshot.builder()
                    .title(entity.getTitle())
                    .startDate(entity.getStartDate())
                    .endDate(entity.getEndDate())
                    .medicationName(afterDetail != null ? afterDetail.getMedicationName() : null)
                    .dosage(afterDetail != null ? afterDetail.getDosage() : null)
                    .frequency(entity.getFrequency())
                    .durationDays(afterDetail != null ? afterDetail.getDurationDays() : null)
                    .instructions(afterDetail != null ? afterDetail.getInstructions() : null)
                    .subType(entity.getSubType().name())
                    .reminderDaysBefore(afterReminder)
                    .build();

            MedicationUpdateDiffDTO diff = MedicationUpdateDiffDTO.builder()
                    .before(before)
                    .after(after)
                    .build();

            return ResponseEntity.ok(ApiResponseGenerator.success(diff));
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
                throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "이미 삭제된 일정입니다.");
            }

            entity.softDelete();
            medicationScheduleService.getCalendarRepository().save(entity);
            return ResponseEntity.ok(ApiResponseGenerator.success(calNo));
    }

    

    

}
