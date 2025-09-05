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
    private String periodType; // DAY, WEEK, MONTH, YEAR
    
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
}
