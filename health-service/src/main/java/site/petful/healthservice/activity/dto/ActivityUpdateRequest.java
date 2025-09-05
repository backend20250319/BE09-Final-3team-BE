package site.petful.healthservice.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.petful.healthservice.activity.enums.ActivityLevel;
import site.petful.healthservice.activity.enums.MealType;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityUpdateRequest {
    
    // 선택적 필드들 (null이면 수정하지 않음)
    private LocalDate activityDate;
    
    @DecimalMin(value = "0.0", message = "산책 거리는 0 이상이어야 합니다")
    private Double walkingDistanceKm;
    
    private ActivityLevel activityLevel;
    
    @DecimalMin(value = "0.1", message = "무게는 0.1 이상이어야 합니다")
    private Double weightKg;
    
    @DecimalMin(value = "0.0", message = "수면 시간은 0 이상이어야 합니다")
    @DecimalMax(value = "24.0", message = "수면 시간은 24 이하여야 합니다")
    private Double sleepHours;
    
    @Min(value = 0, message = "대변 횟수는 0 이상이어야 합니다")
    private Integer poopCount;
    
    @Min(value = 0, message = "소변 횟수는 0 이상이어야 합니다")
    private Integer peeCount;
    
    private String memo;
    
    // 식사 정보 수정 (null이면 기존 식사 정보 유지, 빈 배열이면 모든 식사 정보 삭제)
    private List<MealUpdateRequest> meals;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealUpdateRequest {
        private Long mealNo; // 기존 식사 수정 시 필요 (null이면 새로 추가)
        
        @DecimalMin(value = "0.1", message = "총 무게는 0.1 이상이어야 합니다")
        private Double totalWeightG;
        
        @Min(value = 0, message = "총 칼로리는 0 이상이어야 합니다")
        private Integer totalCalories;
        
        @DecimalMin(value = "0.1", message = "섭취한 무게는 0.1 이상이어야 합니다")
        private Double consumedWeightG;
        
        @Min(value = 0, message = "섭취한 칼로리는 0 이상이어야 합니다")
        private Integer consumedCalories;
        
        private MealType mealType;
        
        // 삭제 여부 (true면 해당 식사 정보 삭제)
        @Builder.Default
        private Boolean isDeleted = false;
    }
}
