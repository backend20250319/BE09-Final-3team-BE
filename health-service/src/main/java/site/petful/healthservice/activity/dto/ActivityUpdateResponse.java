package main.java.site.petful.healthservice.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.healthservice.activity.enums.ActivityLevel;
import site.petful.healthservice.activity.enums.MealType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityUpdateResponse {
    
    private Long activityNo;
    private Long userNo;
    private Long petNo;
    
    private ActivityData before;

    private ActivityData after;
    
    private LocalDateTime updatedAt;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityData {
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
        private List<MealData> meals;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealData {
        private Long mealNo;
        private Double totalWeightG;
        private Integer totalCalories;
        private Double consumedWeightG;
        private Integer consumedCalories;
        private MealType mealType;
    }
}
