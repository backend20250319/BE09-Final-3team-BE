package site.petful.healthservice.activity.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.petful.healthservice.activity.dto.ActivityRequest;
import site.petful.healthservice.activity.dto.ActivityResponse;
import site.petful.healthservice.activity.dto.ActivityChartResponse;
import site.petful.healthservice.activity.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/activity")
@RequiredArgsConstructor
public class ActivityController {
    
    private final ActivityService activityService;
    
    /**
     * 활동 데이터 등록
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Long>> createActivity(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @Valid @RequestBody ActivityRequest request
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        

        request = ActivityRequest.builder()
                .userNo(effectiveUserNo)
                .petNo(request.getPetNo())
                .activityDate(request.getActivityDate())
                .walkingDistanceKm(request.getWalkingDistanceKm())
                .activityLevel(request.getActivityLevel())
                .weightKg(request.getWeightKg())
                .sleepHours(request.getSleepHours())
                .poopCount(request.getPoopCount())
                .peeCount(request.getPeeCount())
                .memo(request.getMemo())
                .meals(request.getMeals())
                .build();
        
        Long activityNo = activityService.createActivity(effectiveUserNo, request);
        return ResponseEntity.ok(ApiResponseGenerator.success(activityNo));
    }
    
    /**
     * 활동 데이터 조회 (특정 날짜)
     */
    @GetMapping("/read")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivity(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @RequestParam("petNo") Long petNo,
            @RequestParam("activityDate") String activityDate
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        ActivityResponse response = activityService.getActivity(effectiveUserNo, petNo, activityDate);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }
    
    /**
     * 스케줄에서 기록이 있는 날짜 목록 조회
     */
    @GetMapping("/schedule")
    public ResponseEntity<ApiResponse<List<String>>> getActivitySchedule(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @RequestParam("petNo") Long petNo,
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        List<String> dates = activityService.getActivitySchedule(effectiveUserNo, petNo, year, month);
        return ResponseEntity.ok(ApiResponseGenerator.success(dates));
    }
    
    /**
     * 활동 데이터 차트 시각화 조회
     * periodType: DAY(일), WEEK(주), MONTH(월), YEAR(년)
     */
    @GetMapping("/chart")
    public ResponseEntity<ApiResponse<ActivityChartResponse>> getActivityChart(
            @RequestHeader(value = "X-User-Id", required = false) Long userNo,
            @RequestParam("petNo") Long petNo,
            @RequestParam("periodType") String periodType,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    ) {
        Long effectiveUserNo = (userNo != null) ? userNo : 1L;
        ActivityChartResponse response = activityService.getActivityChartData(effectiveUserNo, petNo, periodType, startDate, endDate);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }
}
