package site.petful.healthservice.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.healthservice.activity.enums.ActivityLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {
    
    private Long activityNo;
    private Long userNo;
    private Long petNo;
    private LocalDate activityDate;
    private Double walkingDistanceKm;
    private ActivityLevel activityLevel;
    private Integer caloriesBurned;
    private Integer recommendedCaloriesBurned;
    private Integer recommendedCaloriesIntake;
    private Double weightKg;
    private Double sleepHours;
    private Integer poopCount;
    private Integer peeCount;
    private String memo;
    private List<MealResponse> meals;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealResponse {
        private Long mealNo;
        private Double totalWeightG;
        private Integer totalCalories;
        private Double consumedWeightG;
        private Integer consumedCalories;
        private String mealType;
        private String memo;
    }
}
