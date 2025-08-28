package site.petful.healthservice.activity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.petful.healthservice.activity.dto.ActivityRequest;
import site.petful.healthservice.activity.dto.ActivityResponse;
import site.petful.healthservice.activity.entity.Activity;
import site.petful.healthservice.activity.entity.ActivityMeal;
import site.petful.healthservice.activity.enums.ActivityLevel;
import site.petful.healthservice.activity.repository.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {
    
    private final ActivityRepository activityRepository;
    
    @Transactional
    public Long createActivity(Long userNo, ActivityRequest request) {
        log.info("활동 데이터 등록 요청: userNo={}, petNo={}, date={}", 
                userNo, request.getPetNo(), request.getActivityDate());
        
        // 같은 날짜에 이미 활동 데이터가 있는지 확인
        if (activityRepository.existsByPetNoAndActivityDate(request.getPetNo(), request.getActivityDate())) {
            throw new IllegalArgumentException("해당 날짜에 이미 활동 데이터가 존재합니다. 하루에 하나의 활동 기록만 가능합니다.");
        }
        
        // 칼로리 자동 계산
        int recommendedCaloriesBurned = calculateRecommendedCaloriesBurned(request.getWeightKg(), request.getActivityLevel());
        int caloriesBurned = calculateCaloriesBurned(request.getWalkingDistanceKm(), request.getActivityLevel());
        
        // Activity 엔티티 생성
        Activity activity = Activity.builder()
                .userNo(userNo)
                .petNo(request.getPetNo())
                .activityDate(request.getActivityDate())
                .walkingDistanceKm(request.getWalkingDistanceKm())
                .activityLevel(request.getActivityLevel())
                .caloriesBurned(caloriesBurned)
                .recommendedCaloriesBurned(recommendedCaloriesBurned)
                .weightKg(request.getWeightKg())
                .sleepHours(request.getSleepHours())
                .poopCount(request.getPoopCount())
                .peeCount(request.getPeeCount())
                .memo(request.getMemo())
                .build();
        
        // 식사 정보 추가
        request.getMeals().forEach(mealRequest -> {
            // 섭취 칼로리 자동 계산
            int consumedCalories = calculateConsumedCalories(mealRequest.getTotalCalories(), mealRequest.getTotalWeightG(), mealRequest.getConsumedWeightG());
            
            ActivityMeal meal = ActivityMeal.builder()
                    .totalWeightG(mealRequest.getTotalWeightG())
                    .totalCalories(mealRequest.getTotalCalories())
                    .consumedWeightG(mealRequest.getConsumedWeightG())
                    .consumedCalories(consumedCalories)
                    .mealType(mealRequest.getMealType())
                    .memo(mealRequest.getMemo())
                    .build();
            
            activity.addMeal(meal);
        });
        
        // 저장
        Activity savedActivity = activityRepository.save(activity);
        
        log.info("활동 데이터 등록 완료: activityNo={}", savedActivity.getActivityNo());
        
        return savedActivity.getActivityNo();
    }
    
    // 권장 소모 칼로리 = 무게(kg) × 활동계수 × 70
    private int calculateRecommendedCaloriesBurned(Double weightKg, ActivityLevel activityLevel) {
        return (int) Math.round(weightKg * activityLevel.getValue() * 70);
    }
    
    // 소모 칼로리 = 산책 거리(km) × 활동계수 × 5
    private int calculateCaloriesBurned(Double walkingDistanceKm, ActivityLevel activityLevel) {
        return (int) Math.round(walkingDistanceKm * activityLevel.getValue() * 5);
    }
    
    // 섭취 칼로리 = 칼로리/그램 × 섭취량(g)
    private int calculateConsumedCalories(Integer totalCalories, Double totalWeightG, Double consumedWeightG) {
        double caloriesPerGram = (double) totalCalories / totalWeightG;
        return (int) Math.round(caloriesPerGram * consumedWeightG);
    }
    
    // 권장 섭취 칼로리 = 무게(kg) × 활동계수 × 100
    private int calculateRecommendedCaloriesIntake(Double weightKg, ActivityLevel activityLevel) {
        return (int) Math.round(weightKg * activityLevel.getValue() * 100);
    }
    
    /**
     * 특정 날짜의 활동 데이터 조회
     */
    public ActivityResponse getActivity(Long userNo, Long petNo, String activityDateStr) {
        LocalDate activityDate = LocalDate.parse(activityDateStr);
        
        Activity activity = activityRepository.findByPetNoAndActivityDate(petNo, activityDate)
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜의 활동 데이터가 존재하지 않습니다."));
        
        // 사용자 권한 확인
        if (!activity.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("해당 활동 데이터에 대한 접근 권한이 없습니다.");
        }
        
        return convertToResponse(activity);
    }
    
    /**
     * 스케줄에서 기록이 있는 날짜 목록 조회
     */
    public List<String> getActivitySchedule(Long userNo, Long petNo, int year, int month) {
        // 해당 월의 시작일과 마지막일 계산
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        List<Activity> activities = activityRepository.findByPetNoAndDateRange(petNo, startDate, endDate);
        
        // 사용자 권한 확인 및 날짜만 추출
        return activities.stream()
                .filter(activity -> activity.getUserNo().equals(userNo))
                .map(activity -> activity.getActivityDate().toString())
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Activity 엔티티를 Response DTO로 변환
     */
    private ActivityResponse convertToResponse(Activity activity) {
        return ActivityResponse.builder()
                .activityNo(activity.getActivityNo())
                .userNo(activity.getUserNo())
                .petNo(activity.getPetNo())
                .activityDate(activity.getActivityDate())
                .walkingDistanceKm(activity.getWalkingDistanceKm())
                .activityLevel(activity.getActivityLevel())
                .caloriesBurned(activity.getCaloriesBurned())
                .recommendedCaloriesBurned(activity.getRecommendedCaloriesBurned())
                .recommendedCaloriesIntake(calculateRecommendedCaloriesIntake(activity.getWeightKg(), activity.getActivityLevel()))
                .weightKg(activity.getWeightKg())
                .sleepHours(activity.getSleepHours())
                .poopCount(activity.getPoopCount())
                .peeCount(activity.getPeeCount())
                .memo(activity.getMemo())
                .meals(activity.getMeals().stream()
                        .map(meal -> ActivityResponse.MealResponse.builder()
                                .mealNo(meal.getMealNo())
                                .totalWeightG(meal.getTotalWeightG())
                                .totalCalories(meal.getTotalCalories())
                                .consumedWeightG(meal.getConsumedWeightG())
                                .consumedCalories(meal.getConsumedCalories())
                                .mealType(meal.getMealType())
                                .memo(meal.getMemo())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .build();
    }
}
