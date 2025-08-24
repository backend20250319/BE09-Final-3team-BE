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
import site.petful.healthservice.medical.dto.CareRequestDTO;
import site.petful.healthservice.medical.service.CareScheduleService;
import site.petful.healthservice.common.enums.CalendarSubType;
import site.petful.healthservice.common.enums.CareFrequency;

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


