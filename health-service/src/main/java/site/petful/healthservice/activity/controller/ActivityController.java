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
import site.petful.healthservice.common.response.ErrorCode;

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
            @RequestAttribute("X-User-No") Long userNo,
            @Valid @RequestBody ActivityRequest request
    ) {
        if (userNo == null) {
            return ResponseEntity.badRequest().body(ApiResponseGenerator.failGeneric(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."));
        }

        request = ActivityRequest.builder()
                .userNo(userNo)
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
        
        Long activityNo = activityService.createActivity(userNo, request);
        return ResponseEntity.ok(ApiResponseGenerator.success(activityNo));
    }
    
    /**
     * 활동 데이터 조회 (특정 날짜)
     */
    @GetMapping("/read")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivity(
            @RequestAttribute("X-User-No") Long userNo,
            @RequestParam("petNo") Long petNo,
            @RequestParam("activityDate") String activityDate
    ) {
        if (userNo == null) {
            return ResponseEntity.badRequest().body(ApiResponseGenerator.failGeneric(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."));
        }
        
        ActivityResponse response = activityService.getActivity(userNo, petNo, activityDate);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }
    
    /**
     * 스케줄에서 기록이 있는 날짜 목록 조회
     */
    @GetMapping("/schedule")
    public ResponseEntity<ApiResponse<List<String>>> getActivitySchedule(
            @RequestAttribute("X-User-No") Long userNo,
            @RequestParam("petNo") Long petNo,
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {
        if (userNo == null) {
            return ResponseEntity.badRequest().body(ApiResponseGenerator.failGeneric(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."));
        }
        
        List<String> dates = activityService.getActivitySchedule(userNo, petNo, year, month);
        return ResponseEntity.ok(ApiResponseGenerator.success(dates));
    }
    
    /**
     * 활동 데이터 차트 시각화 조회
     * periodType: DAY(일), WEEK(주), MONTH(월), YEAR(년)
     */
    @GetMapping("/chart")
    public ResponseEntity<ApiResponse<ActivityChartResponse>> getActivityChart(
            @RequestAttribute("X-User-No") Long userNo,
            @RequestParam("petNo") Long petNo,
            @RequestParam("periodType") String periodType,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    ) {
        if (userNo == null) {
            return ResponseEntity.badRequest().body(ApiResponseGenerator.failGeneric(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."));
        }
        
        ActivityChartResponse response = activityService.getActivityChartData(userNo, petNo, periodType, startDate, endDate);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }
}
