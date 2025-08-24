package site.petful.healthservice.medical.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;
import site.petful.healthservice.medical.dto.CareRequestDTO;
import site.petful.healthservice.medical.service.CareScheduleService;

@RestController
@RequestMapping("/care")
@RequiredArgsConstructor
public class CareController {

    private final CareScheduleService careScheduleService;

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


