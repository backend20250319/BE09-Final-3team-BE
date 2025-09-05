package site.petful.healthservice.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.petful.healthservice.activity.enums.ActivityLevel;
import site.petful.healthservice.activity.enums.MealType;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRequest {
    
    private Long userNo;
    
    @NotNull(message = "반려동물 번호는 필수입니다")
    private Long petNo;
    
    @NotNull(message = "활동 날짜는 필수입니다")
    private LocalDate activityDate;
    
    @NotNull(message = "산책 거리는 필수입니다")
    @DecimalMin(value = "0.0", message = "산책 거리는 0 이상이어야 합니다")
    private Double walkingDistanceKm;
    
    @NotNull(message = "활동 계수는 필수입니다")
    private ActivityLevel activityLevel;
    
    @NotNull(message = "무게는 필수입니다")
    @DecimalMin(value = "0.1", message = "무게는 0.1 이상이어야 합니다")
    private Double weightKg;
    
    @NotNull(message = "수면 시간은 필수입니다")
    @DecimalMin(value = "0.0", message = "수면 시간은 0 이상이어야 합니다")
    @DecimalMax(value = "24.0", message = "수면 시간은 24 이하여야 합니다")
    private Double sleepHours;
    
    @NotNull(message = "대변 횟수는 필수입니다")
    @Min(value = 0, message = "대변 횟수는 0 이상이어야 합니다")
    private Integer poopCount;
    
    @NotNull(message = "소변 횟수는 필수입니다")
    @Min(value = 0, message = "소변 횟수는 0 이상이어야 합니다")
    private Integer peeCount;
    
    private String memo;
    
    @NotEmpty(message = "식사 정보는 최소 1개 이상 필요합니다")
    private List<MealRequest> meals;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealRequest {
        
        @NotNull(message = "총 무게는 필수입니다")
        @DecimalMin(value = "0.1", message = "총 무게는 0.1 이상이어야 합니다")
        private Double totalWeightG;
        
        @NotNull(message = "총 칼로리는 필수입니다")
        @Min(value = 0, message = "총 칼로리는 0 이상이어야 합니다")
        private Integer totalCalories;
        
        @NotNull(message = "섭취한 무게는 필수입니다")
        @DecimalMin(value = "0.1", message = "섭취한 무게는 0.1 이상이어야 합니다")
        private Double consumedWeightG;
        
        @NotNull(message = "섭취한 칼로리는 필수입니다")
        @Min(value = 0, message = "섭취한 칼로리는 0 이상이어야 합니다")
        private Integer consumedCalories;
        
        @NotNull(message = "식사 타입은 필수입니다")
        private MealType mealType;
    }
}
