package site.petful.healthservice.activity.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.petful.healthservice.activity.dto.ActivityRequest;
import site.petful.healthservice.activity.dto.ActivityResponse;
import site.petful.healthservice.activity.dto.ActivityChartResponse;
import site.petful.healthservice.activity.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;
import site.petful.healthservice.common.response.ErrorCode;

import jakarta.validation.Valid;
import java.util.List;
import site.petful.healthservice.common.dto.PetResponse;
import site.petful.healthservice.activity.dto.ActivityLevelResponse;
import site.petful.healthservice.activity.enums.ActivityLevel;
import site.petful.healthservice.activity.dto.ActivityUpdateRequest;
import site.petful.healthservice.activity.dto.ActivityUpdateResponse;
import site.petful.healthservice.activity.dto.ActivitySummaryResponse;

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
            @AuthenticationPrincipal String userNo,
            @Valid @RequestBody ActivityRequest request
    ) {
        request = ActivityRequest.builder()
                .userNo(Long.valueOf(userNo))
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
        
        Long activityNo = activityService.createActivity(Long.valueOf(userNo), request);
        return ResponseEntity.ok(ApiResponseGenerator.success(activityNo));
    }
    
    /**
     * 활동 데이터 조회 (특정 날짜)
     */
    @GetMapping("/read")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivity(
            @AuthenticationPrincipal String userNo,
            @RequestParam("petNo") Long petNo,
            @RequestParam("activityDate") String activityDate
    ) {
        ActivityResponse response = activityService.getActivity(Long.valueOf(userNo), petNo, activityDate);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }
    
    /**
     * 스케줄에서 기록이 있는 날짜 목록 조회
     */
    @GetMapping("/schedule")
    public ResponseEntity<ApiResponse<List<String>>> getActivitySchedule(
            @AuthenticationPrincipal String userNo,
            @RequestParam("petNo") Long petNo,
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {
        List<String> dates = activityService.getActivitySchedule(Long.valueOf(userNo), petNo, year, month);
        return ResponseEntity.ok(ApiResponseGenerator.success(dates));
    }
    
    /**
     * 사용자별 펫 프로필 목록 조회
     */
    @GetMapping("/pets")
    public ResponseEntity<ApiResponse<List<PetResponse>>> getUserPets(
            @AuthenticationPrincipal String userNo
    ) {
        List<PetResponse> pets = activityService.getUserPets(Long.valueOf(userNo));
        return ResponseEntity.ok(ApiResponseGenerator.success(pets));
    }

    /**
     * 활동량 옵션 목록 조회
     */
    @GetMapping("/activity-levels")
    public ResponseEntity<ApiResponse<List<ActivityLevelResponse>>> getActivityLevels() {
        List<ActivityLevelResponse> levels = List.of(
            ActivityLevelResponse.builder()
                .value("LOW")
                .label("거의 안 움직여요")
                .numericValue(1.2)
                .build(),
            ActivityLevelResponse.builder()
                .value("MEDIUM_LOW")
                .label("가끔 산책해요")
                .numericValue(1.5)
                .build(),
            ActivityLevelResponse.builder()
                .value("MEDIUM_HIGH")
                .label("자주 뛰어놀아요")
                .numericValue(1.7)
                .build(),
            ActivityLevelResponse.builder()
                .value("HIGH")
                .label("매우 활동적이에요")
                .numericValue(1.9)
                .build()
        );
        return ResponseEntity.ok(ApiResponseGenerator.success(levels));
    }

    /**
     * 활동 데이터 차트 시각화 조회
     * periodType: DAY(일), WEEK(주), MONTH(월), YEAR(년)
     * startDate, endDate가 없으면 periodType에 따른 기본 기간 사용
     */
    @GetMapping("/chart")
    public ResponseEntity<ApiResponse<ActivityChartResponse>> getActivityChart(
            @AuthenticationPrincipal String userNo,
            @RequestParam("petNo") Long petNo,
            @RequestParam("periodType") String periodType,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        ActivityChartResponse response = activityService.getActivityChartData(Long.valueOf(userNo), petNo, periodType, startDate, endDate);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }

    /**
     * 기간별 활동 데이터 요약 조회
     * periodType: TODAY, LAST_3_DAYS, LAST_7_DAYS, THIS_WEEK, THIS_MONTH, CUSTOM
     * CUSTOM 타입일 때만 startDate, endDate 파라미터 필요
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ActivitySummaryResponse>> getActivitySummary(
            @AuthenticationPrincipal String userNo,
            @RequestParam("petNo") Long petNo,
            @RequestParam("periodType") String periodType,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        ActivitySummaryResponse response = activityService.getActivitySummary(Long.valueOf(userNo), petNo, periodType, startDate, endDate);
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }

    /**
     * 활동 데이터 부분 수정 (PATCH)
     */
    @PatchMapping("/update/{activityNo}")
    public ResponseEntity<ApiResponse<ActivityUpdateResponse>> updateActivity(
            @AuthenticationPrincipal String userNo,
            @PathVariable Long activityNo,
            @RequestBody ActivityUpdateRequest request
    ) {
        log.info("활동 데이터 부분 수정 요청: userNo={}, activityNo={}, request={}", userNo, activityNo, request);
        
        ActivityUpdateResponse response = activityService.updateActivity(Long.valueOf(userNo), activityNo, request);
        
        log.info("활동 데이터 부분 수정 완료: activityNo={}, 수정 전후 데이터 포함", activityNo);
        
        return ResponseEntity.ok(ApiResponseGenerator.success(response));
    }
}
