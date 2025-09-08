package site.petful.healthservice.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityChartResponse {
    
    private List<ChartData> chartData;
    private SummaryStats summaryStats;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        private String date; // 날짜 (YYYY-MM-DD 또는 YYYY-MM 또는 YYYY)
        private String displayDate; // 표시용 날짜 (월, 화, 수, 목, 금, 토, 일 또는 1월, 2월 등)
        
        // 산책 소모 칼로리
        private Integer recommendedCaloriesBurned;
        private Integer actualCaloriesBurned;
        
        // 섭취 칼로리
        private Integer recommendedCaloriesIntake;
        private Integer actualCaloriesIntake;
        
        // 배변 횟수
        private Integer poopCount;
        private Integer peeCount;
        
        // 수면 시간
        private Double sleepHours;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryStats {
        private Integer totalDays; // 총 활동 일수
        private Double totalWalkingDistance; // 총 산책 거리 (km)
        private Integer totalCaloriesBurned; // 총 소모 칼로리
        private Integer totalCaloriesIntake; // 총 섭취 칼로리
        private Double averageSleepHours; // 평균 수면 시간
        private Integer totalPoopCount; // 총 대변 횟수
        private Integer totalPeeCount; // 총 소변 횟수
        
        // 평균값들
        private Double averageWalkingDistance; // 평균 산책 거리
        private Double averageCaloriesBurned; // 평균 소모 칼로리
        private Double averageCaloriesIntake; // 평균 섭취 칼로리
        private Double averagePoopCount; // 평균 대변 횟수
        private Double averagePeeCount; // 평균 소변 횟수
    }
}
