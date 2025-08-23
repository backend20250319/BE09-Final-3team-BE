package site.petful.snsservice.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class DateTimeUtils {

    public static void main(String[] args) {
        LocalDate testDate = LocalDate.of(2025, 8, 10); // 31일까지 있는 8월로 테스트

        // 각 메소드가 반환하는 유닉스 시간을 다시 날짜로 변환해서 확인
        System.out.println("1st Half Start: " + DateTimeUtils.fromUnixTimeToLocalDateTime(
            DateTimeUtils.getFirstHalfOfMonthStart(testDate)));
        System.out.println("1st Half End:   " + DateTimeUtils.fromUnixTimeToLocalDateTime(
            DateTimeUtils.getFirstHalfOfMonthEnd(testDate)));
        System.out.println("2nd Half Start: " + DateTimeUtils.fromUnixTimeToLocalDateTime(
            DateTimeUtils.getSecondHalfOfMonthStart(testDate)));
        System.out.println("2nd Half End:   " + DateTimeUtils.fromUnixTimeToLocalDateTime(
            DateTimeUtils.getSecondHalfOfMonthEnd(testDate)));
    }

// 예상 결과
// 1st Half Start: 2025-08-01T00:00
// 1st Half End:   2025-08-15T23:59:59  <-- 여기가 2025-08-31로 나오는지 확인!
// 2nd Half Start: 2025-08-16T00:00
// 2nd Half End:   2025-08-31T23:59:59

    public static long getStartOfCurrentMonthAsUnixTime() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return toUnixTimestamp(startOfMonth);
    }

    public static long getEndOfCurrentMonthAsUnixTime() {
        LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
        return toUnixTimestamp(endOfMonth);
    }

    public static LocalDateTime fromUnixTimeToLocalDateTime(long unixTimestamp) {
        return Instant.ofEpochSecond(unixTimestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }

    /**
     * 지정된 월의 첫 번째 반(1일~15일)의 시작 시간을 Unix timestamp로 반환
     */
    public static long getFirstHalfOfMonthStart(LocalDate date) {
        LocalDateTime startOfFirstHalf = date.withDayOfMonth(1).atStartOfDay();
        return toUnixTimestamp(startOfFirstHalf);
    }

    /**
     * 지정된 월의 첫 번째 반(1일~15일)의 끝 시간을 Unix timestamp로 반환
     */
    public static long getFirstHalfOfMonthEnd(LocalDate date) {
        LocalDateTime endOfFirstHalf = date.withDayOfMonth(15).atTime(23, 59, 59);
        return toUnixTimestamp(endOfFirstHalf);
    }

    /**
     * 지정된 월의 두 번째 반(16일~월말)의 시작 시간을 Unix timestamp로 반환
     */
    public static long getSecondHalfOfMonthStart(LocalDate date) {
        LocalDateTime startOfSecondHalf = date.withDayOfMonth(16).atStartOfDay();
        return toUnixTimestamp(startOfSecondHalf);
    }

    /**
     * 지정된 월의 두 번째 반(16일~월말)의 끝 시간을 Unix timestamp로 반환
     */
    public static long getSecondHalfOfMonthEnd(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        LocalDateTime endOfSecondHalf = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return toUnixTimestamp(endOfSecondHalf);
    }

    private static long toUnixTimestamp(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}