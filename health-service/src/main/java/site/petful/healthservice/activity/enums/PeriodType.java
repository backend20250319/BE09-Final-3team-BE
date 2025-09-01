package site.petful.healthservice.activity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Getter
@RequiredArgsConstructor
public enum PeriodType {
    
    CUSTOM("사용자 지정"),
    TODAY("오늘"),
    LAST_3_DAYS("최근 3일"),
    LAST_7_DAYS("최근 7일"),
    THIS_WEEK("이번 주"),
    THIS_MONTH("이번 달");
    
    private final String description;
    
    /**
     * 기간 타입에 따른 시작일과 종료일 계산
     */
    public DateRange calculateDateRange() {
        LocalDate today = LocalDate.now();
        
        return switch (this) {
            case TODAY -> new DateRange(today, today);
            case LAST_3_DAYS -> new DateRange(today.minusDays(2), today);
            case LAST_7_DAYS -> new DateRange(today.minusDays(6), today);
            case THIS_WEEK -> {
                LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                yield new DateRange(startOfWeek, today);
            }
            case THIS_MONTH -> {
                LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
                yield new DateRange(startOfMonth, today);
            }
            case CUSTOM -> throw new IllegalArgumentException("CUSTOM 타입은 시작일과 종료일을 직접 지정해야 합니다.");
        };
    }
    
    /**
     * 날짜 범위를 나타내는 내부 클래스
     */
    public static class DateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;
        
        public DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        public LocalDate getStartDate() {
            return startDate;
        }
        
        public LocalDate getEndDate() {
            return endDate;
        }
    }
}
