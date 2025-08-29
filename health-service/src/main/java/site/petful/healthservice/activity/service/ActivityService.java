package site.petful.healthservice.activity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.petful.healthservice.activity.dto.ActivityRequest;
import site.petful.healthservice.activity.dto.ActivityResponse;
import site.petful.healthservice.activity.dto.ActivityChartResponse;
import site.petful.healthservice.activity.entity.Activity;
import site.petful.healthservice.activity.entity.ActivityMeal;
import site.petful.healthservice.activity.enums.ActivityLevel;
import site.petful.healthservice.activity.repository.ActivityRepository;
import site.petful.healthservice.common.exception.BusinessException;
import site.petful.healthservice.common.response.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.petful.healthservice.common.client.PetServiceClient;
import site.petful.healthservice.common.dto.PetResponse;
import site.petful.healthservice.common.response.ApiResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {
    
    private final ActivityRepository activityRepository;
    private final PetServiceClient petServiceClient;
    
    @Transactional
    public Long createActivity(Long userNo, ActivityRequest request) {

        
        // 펫 소유권 검증
        if (!isPetOwnedByUser(request.getPetNo(), userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 펫에 대한 접근 권한이 없습니다.");
        }
        
        // 같은 날짜에 이미 활동 데이터가 있는지 확인
        if (activityRepository.existsByPetNoAndActivityDate(request.getPetNo(), request.getActivityDate())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "해당 날짜에 이미 활동 데이터가 존재합니다. 하루에 하나의 활동 기록만 가능합니다.");
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
            int consumedCalories = calculateConsumedCalories(
                mealRequest.getTotalCalories(),
                mealRequest.getTotalWeightG(),
                mealRequest.getConsumedWeightG()
            );
            
            activity.addMeal(ActivityMeal.builder()
                .totalWeightG(mealRequest.getTotalWeightG())
                .totalCalories(mealRequest.getTotalCalories())
                .consumedWeightG(mealRequest.getConsumedWeightG())
                .consumedCalories(consumedCalories)
                .mealType(mealRequest.getMealType())
                .memo(mealRequest.getMemo())
                .build());
        });
        
        Activity savedActivity = activityRepository.save(activity);
        
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

        
        // 펫 소유권 검증
        if (!isPetOwnedByUser(petNo, userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 펫에 대한 접근 권한이 없습니다.");
        }
        
        LocalDate activityDate = LocalDate.parse(activityDateStr);
        
        Activity activity = activityRepository.findByPetNoAndActivityDate(petNo, activityDate)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "해당 날짜의 활동 데이터가 존재하지 않습니다."));
        
        // 사용자 권한 확인
        if (!activity.getUserNo().equals(userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 활동 데이터에 대한 접근 권한이 없습니다.");
        }
        
        return convertToResponse(activity);
    }
    
    /**
     * 스케줄에서 기록이 있는 날짜 목록 조회
     */
    public List<String> getActivitySchedule(Long userNo, Long petNo, int year, int month) {

        // 펫 소유권 검증
        if (!isPetOwnedByUser(petNo, userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 펫에 대한 접근 권한이 없습니다.");
        }
        
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
     * 차트 데이터 조회 (일/주/월/년 단위)
     */
    public ActivityChartResponse getActivityChartData(Long userNo, Long petNo, String periodType, String startDateStr, String endDateStr) {

        // 펫 소유권 검증
        if (!isPetOwnedByUser(petNo, userNo)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 펫에 대한 접근 권한이 없습니다.");
        }
        
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        
        List<Activity> activities = activityRepository.findByPetNoAndDateRange(petNo, startDate, endDate);
        
        // 사용자 권한 확인
        activities = activities.stream()
                .filter(activity -> activity.getUserNo().equals(userNo))
                .collect(Collectors.toList());
        
        List<ActivityChartResponse.ChartData> chartDataList = activities.stream()
                .map(activity -> convertToChartData(activity, periodType))
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
        
        return ActivityChartResponse.builder()
                .chartData(chartDataList)
                .periodType(periodType)
                .build();
    }
    
    /**
     * Activity 엔티티를 ChartData DTO로 변환
     */
    private ActivityChartResponse.ChartData convertToChartData(Activity activity, String periodType) {
        String displayDate = getDisplayDate(activity.getActivityDate(), periodType);
        
        return ActivityChartResponse.ChartData.builder()
                .date(activity.getActivityDate().toString())
                .displayDate(displayDate)
                .recommendedCaloriesBurned(activity.getRecommendedCaloriesBurned())
                .actualCaloriesBurned(activity.getCaloriesBurned())
                .recommendedCaloriesIntake(calculateRecommendedCaloriesIntake(activity.getWeightKg(), activity.getActivityLevel()))
                .actualCaloriesIntake(calculateTotalConsumedCalories(activity))
                .poopCount(activity.getPoopCount())
                .peeCount(activity.getPeeCount())
                .sleepHours(activity.getSleepHours())
                .build();
    }
    
    /**
     * 표시용 날짜 문자열 생성
     */
    private String getDisplayDate(LocalDate date, String periodType) {
        return switch (periodType.toUpperCase()) {
            case "DAY" -> getDayOfWeekKorean(date);
            case "WEEK" -> getWeekDisplay(date);
            case "MONTH" -> getMonthDisplay(date);
            case "YEAR" -> getYearDisplay(date);
            default -> date.toString();
        };
    }
    
    private String getDayOfWeekKorean(LocalDate date) {
        String[] days = {"월", "화", "수", "목", "금", "토", "일"};
        return days[date.getDayOfWeek().getValue() - 1];
    }
    
    private String getWeekDisplay(LocalDate date) {
        return date.getMonthValue() + "월 " + date.getDayOfMonth() + "일";
    }
    
    private String getMonthDisplay(LocalDate date) {
        return date.getMonthValue() + "월";
    }
    
    private String getYearDisplay(LocalDate date) {
        return String.valueOf(date.getYear());
    }
    
    /**
     * 총 섭취 칼로리 계산
     */
    private Integer calculateTotalConsumedCalories(Activity activity) {
        return activity.getMeals().stream()
                .mapToInt(ActivityMeal::getConsumedCalories)
                .sum();
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

    /**
     * 사용자별 펫 프로필 목록 조회
     */
    public List<PetResponse> getUserPets(Long userNo) {

        try {
            ApiResponse<List<PetResponse>> petsResponse = petServiceClient.getPetsByUser(userNo);
            
            if (petsResponse != null && petsResponse.getData() != null) {
                return petsResponse.getData();
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("사용자 펫 목록 조회 중 예외 발생: userNo={}", userNo, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "펫 목록 조회 중 오류가 발생했습니다.");
        }
    }

    // 펫 소유권 검증 메서드
    private boolean isPetOwnedByUser(Long petNo, Long userNo) {
        try {
            ApiResponse<PetResponse> petResponse = petServiceClient.getPet(petNo);
            
            if (petResponse != null && petResponse.getData() != null) {
                PetResponse pet = petResponse.getData();
                if (pet.getUserNo() != null) {
                    return pet.getUserNo().equals(userNo);
                }
            }
            return false;
            
        } catch (Exception e) {
            log.error("펫 소유권 검증 중 예외 발생: petNo={}, userNo={}", petNo, userNo, e);
            return false;
        }
    }
}
