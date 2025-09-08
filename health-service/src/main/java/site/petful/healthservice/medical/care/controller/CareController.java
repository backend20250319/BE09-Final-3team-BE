package site.petful.healthservice.medical.care.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;
import site.petful.healthservice.medical.care.dto.*;
import site.petful.healthservice.medical.care.service.CareScheduleService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medical/care")
@RequiredArgsConstructor
public class CareController {

    private final CareScheduleService careScheduleService;

    @GetMapping("/meta")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getMeta() {
        Map<String, List<String>> data = careScheduleService.getCareMeta();
        return ResponseEntity.ok(ApiResponseGenerator.success(data));
    }

    @GetMapping("/read")
    public ResponseEntity<ApiResponse<List<CareResponseDTO>>> readCare(
            @AuthenticationPrincipal String userNo,
            @RequestParam(value = "petNo") Long petNo,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "subType", required = false) String subType
    ) {
        List<CareResponseDTO> result = careScheduleService.listCareSchedules(Long.valueOf(userNo), petNo, from, to, subType);
        return ResponseEntity.ok(ApiResponseGenerator.success(result));
    }

    @GetMapping("/{calNo}")
    public ResponseEntity<ApiResponse<CareDetailDTO>> getCareDetail(
            @AuthenticationPrincipal String userNo,
            @PathVariable("calNo") Long calNo
    ) {
        CareDetailDTO dto = careScheduleService.getCareDetail(calNo, Long.valueOf(userNo));
        return ResponseEntity.ok(ApiResponseGenerator.success(dto));
    }

    /**
     * 돌봄 일정 생성 (캘린더 기반)
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Long>> createCare(
            @AuthenticationPrincipal String userNo,
            @Valid @RequestBody CareRequestDTO request
    ) {
        Long calNo = careScheduleService.createCareSchedule(Long.valueOf(userNo), request);
        return ResponseEntity.ok(ApiResponseGenerator.success(calNo));
    }

    
    /**
     * 돌봄 일정 수정 (부분 업데이트)
     */
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<Long>> updateCare(
            @AuthenticationPrincipal String userNo,
            @RequestParam("calNo") Long calNo,
            @RequestParam(defaultValue = "true") boolean isEditMode,
            @RequestBody CareUpdateRequestDTO request
    ) {
        Long updatedCalNo = careScheduleService.updateCareSchedule(calNo, request, Long.valueOf(userNo), isEditMode);
        return ResponseEntity.ok(ApiResponseGenerator.success(updatedCalNo));
    }

    
    /**
     * 돌봄 일정 삭제 (soft delete)
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Long>> deleteCare(
            @AuthenticationPrincipal String userNo,
            @RequestParam("calNo") Long calNo
    ) {
        Long deletedCalNo = careScheduleService.deleteCareSchedule(calNo, Long.valueOf(userNo));
        return ResponseEntity.ok(ApiResponseGenerator.success(deletedCalNo));
    }


    
    /**
     * 돌봄/접종 일정 알림 활성화/비활성화
     */
    @PatchMapping("/alarm")
    public ResponseEntity<ApiResponse<Boolean>> toggleAlarm(
            @AuthenticationPrincipal String userNo,
            @RequestParam("calNo") Long calNo
    ) {
        Boolean alarmEnabled = careScheduleService.toggleAlarm(calNo, Long.valueOf(userNo));
        return ResponseEntity.ok(ApiResponseGenerator.success(alarmEnabled));
    }


}


