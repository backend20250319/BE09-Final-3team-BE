package site.petful.healthservice.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySummaryResponse {
    
    private Long petNo;
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodType; // CUSTOM, TODAY, LAST_3_DAYS, LAST_7_DAYS, THIS_WEEK, THIS_MONTH
    
    // 요약 통계
    private SummaryStats summaryStats;
    
    // 상세 데이터 목록
    private List<ActivityResponse> activities;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryStats {
        private Integer totalActivities; // 총 활동 횟수
        private Double totalWalkingDistance; // 총 산책 거리 (km)
        private Integer totalCaloriesBurned; // 총 소모 칼로리
        private Integer totalCaloriesIntake; // 총 섭취 칼로리
        private Double averageWeight; // 평균 체중
        private Double averageSleepHours; // 평균 수면 시간
        private Integer totalPoopCount; // 총 대변 횟수
        private Integer totalPeeCount; // 총 소변 횟수
        
        // 평균값들
        private Double averageWalkingDistance; // 평균 산책 거리
        private Double averageCaloriesBurned; // 평균 소모 칼로리
        private Double averageCaloriesIntake; // 평균 섭취 칼로리
        private Double averagePoopCount; // 평균 대변 횟수
        private Double averagePeeCount; // 평균 소변 횟수
        
        // 목표 대비 달성률
        private Double caloriesBurnedAchievementRate; // 소모 칼로리 달성률
        private Double caloriesIntakeAchievementRate; // 섭취 칼로리 달성률
    }
}
