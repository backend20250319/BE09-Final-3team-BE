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
}
