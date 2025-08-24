package site.petful.healthservice.medical.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;
import site.petful.healthservice.medical.dto.*;
import site.petful.healthservice.medical.service.CareScheduleService;

import java.util.List;

@RestController
@RequestMapping("/care")
@RequiredArgsConstructor
public class CareController {

    private final CareScheduleService careScheduleService;

    @GetMapping("/meta")
    public ResponseEntity<ApiResponse<java.util.Map<String, java.util.List<String>>>> getMeta() {
        java.util.Map<String, java.util.List<String>> data = careScheduleService.getCareMeta();
        return ResponseEntity.ok(ApiResponseGenerator.success(data));
    }

    @GetMapping("/read")
    public ResponseEntity<ApiResponse<List<CareResponseDTO>>> readCare(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "subType", required = false) String subType
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        List<CareResponseDTO> result = careScheduleService.listCareSchedules(effectiveUserNo, from, to, subType);
        return ResponseEntity.ok(ApiResponseGenerator.success(result));
    }

    @GetMapping("/{calNo}")
    public ResponseEntity<ApiResponse<CareDetailDTO>> getCareDetail(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @PathVariable("calNo") Long calNo
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        CareDetailDTO dto = careScheduleService.getCareDetail(calNo, effectiveUserNo);
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

    
    /**
     * 돌봄 일정 수정 (부분 업데이트)
     */
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<Long>> updateCare(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @RequestParam("calNo") Long calNo,
            @RequestBody CareUpdateRequestDTO request
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        Long updatedCalNo = careScheduleService.updateCareSchedule(calNo, request, effectiveUserNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(updatedCalNo));
    }

    // ==================== 돌봄 일정 삭제 ====================
    
    /**
     * 돌봄 일정 삭제 (soft delete)
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Long>> deleteCare(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @RequestParam("calNo") Long calNo
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        Long deletedCalNo = careScheduleService.deleteCareSchedule(calNo, effectiveUserNo);
        return ResponseEntity.ok(ApiResponseGenerator.success(deletedCalNo));
    }
}


