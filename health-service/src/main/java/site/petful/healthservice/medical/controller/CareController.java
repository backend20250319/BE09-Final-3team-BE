package site.petful.healthservice.medical.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;
import site.petful.healthservice.common.response.ErrorCode;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.medical.dto.CareRequestDTO;
import site.petful.healthservice.medical.dto.CareResponseDTO;
import site.petful.healthservice.medical.dto.CareDetailDTO;
import site.petful.healthservice.medical.service.CareScheduleService;
import site.petful.healthservice.common.enums.CalendarSubType;
import site.petful.healthservice.common.enums.CareFrequency;
import site.petful.healthservice.common.entity.Calendar;

@RestController
@RequestMapping("/care")
@RequiredArgsConstructor
public class CareController {

    private final CareScheduleService careScheduleService;

    @GetMapping("/meta")
    public ResponseEntity<ApiResponse<java.util.Map<String, java.util.List<String>>>> getMeta() {
        java.util.List<String> subTypes = java.util.Arrays.stream(CalendarSubType.values())
                .filter(CalendarSubType::isCareType)
                .map(Enum::name)
                .toList();
        java.util.List<String> frequencies = java.util.Arrays.stream(CareFrequency.values())
                .map(Enum::name)
                .toList();
        java.util.Map<String, java.util.List<String>> data = new java.util.HashMap<>();
        data.put("subTypes", subTypes);
        data.put("frequencies", frequencies);
        return ResponseEntity.ok(ApiResponseGenerator.success(data));
    }

    @GetMapping("/read")
    public ResponseEntity<ApiResponse<java.util.List<CareResponseDTO>>> readCare(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @org.springframework.web.bind.annotation.RequestParam(value = "from", required = false) String from,
            @org.springframework.web.bind.annotation.RequestParam(value = "to", required = false) String to,
            @org.springframework.web.bind.annotation.RequestParam(value = "subType", required = false) String subType
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;

        java.time.LocalDateTime start;
        java.time.LocalDateTime end;
        try {
            start = (from == null || from.isBlank())
                    ? java.time.LocalDate.now().minusMonths(1).atStartOfDay()
                    : java.time.LocalDate.parse(from).atStartOfDay();
            end = (to == null || to.isBlank())
                    ? java.time.LocalDate.now().plusMonths(1).atTime(23, 59, 59)
                    : java.time.LocalDate.parse(to).atTime(23, 59, 59);
        } catch (java.time.format.DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_DATE_FORMAT, "유효하지 않은 날짜 형식입니다.");
        }
        if (start.isAfter(end)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE, "from이 to보다 늦을 수 없습니다.");
        }

        java.util.List<Calendar> items = careScheduleService.getCalendarRepository()
                .findByUserNoAndDateRange(effectiveUserNo, start, end);

        java.util.stream.Stream<Calendar> stream = items.stream()
                .filter(c -> c.getMainType() == site.petful.healthservice.common.enums.CalendarMainType.CARE);
        if (subType != null && !subType.isBlank()) {
            stream = stream.filter(c -> c.getSubType().name().equalsIgnoreCase(subType));
        }

        java.util.List<CareResponseDTO> result = stream
                .map(c -> CareResponseDTO.builder()
                        .calNo(c.getCalNo())
                        .title(c.getTitle())
                        .startDate(c.getStartDate())
                        .endDate(c.getEndDate())
                        .mainType(c.getMainType().name())
                        .subType(c.getSubType().name())
                        .frequency(c.getFrequency())
                        .alarmEnabled(c.getReminderDaysBefore() != null && !c.getReminderDaysBefore().isEmpty())
                        .reminderDaysBefore((c.getReminderDaysBefore() == null || c.getReminderDaysBefore().isEmpty()) ? null : c.getReminderDaysBefore().get(0))
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponseGenerator.success(result));
    }

    @GetMapping("/{calNo}")
    public ResponseEntity<ApiResponse<CareDetailDTO>> getCareDetail(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @org.springframework.web.bind.annotation.PathVariable("calNo") Long calNo
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        java.util.Optional<Calendar> opt = careScheduleService.getCalendarRepository().findById(calNo);
        if (opt.isEmpty()) throw new BusinessException(ErrorCode.NOT_FOUND, "일정을 찾을 수 없습니다.");
        Calendar c = opt.get();
        if (!c.getUserNo().equals(effectiveUserNo)) throw new BusinessException(ErrorCode.FORBIDDEN, "본인 일정이 아닙니다.");
        if (Boolean.TRUE.equals(c.getDeleted())) throw new BusinessException(ErrorCode.SCHEDULE_ALREADY_DELETED, "삭제된 일정입니다.");
        if (c.getMainType() != site.petful.healthservice.common.enums.CalendarMainType.CARE) {
            throw new BusinessException(ErrorCode.SCHEDULE_TYPE_MISMATCH, "돌봄 일정이 아닙니다.");
        }

        CareDetailDTO dto = CareDetailDTO.builder()
                .calNo(c.getCalNo())
                .title(c.getTitle())
                .mainType(c.getMainType().name())
                .subType(c.getSubType().name())
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .time(c.getStartDate() != null ? c.getStartDate().toLocalTime() : null)
                .frequency(c.getFrequency())
                .alarmEnabled(c.getReminderDaysBefore() != null && !c.getReminderDaysBefore().isEmpty())
                .reminderDaysBefore(c.getReminderDaysBefore())
                .build();
        return ResponseEntity.ok(ApiResponseGenerator.success(dto));
    }

    /**
     * 돌봄 일정 생성 (캘린더 기반)
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Long>> createCare(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @Valid @RequestBody CareRequestDTO request
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        Long calNo = careScheduleService.createCareSchedule(effectiveUserNo, request);
        return ResponseEntity.ok(ApiResponseGenerator.success(calNo));
    }
}


