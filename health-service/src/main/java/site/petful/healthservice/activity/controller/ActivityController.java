package site.petful.healthservice.activity.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.petful.healthservice.activity.dto.ActivityRequest;
import site.petful.healthservice.activity.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.petful.healthservice.common.response.ApiResponse;
import site.petful.healthservice.common.response.ApiResponseGenerator;

import jakarta.validation.Valid;

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
}
