package site.petful.snsservice.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class DateTimeUtils {

    public static LocalDateTime getStartOfCurrentMonth() {
        return LocalDate.now().withDayOfMonth(1).atStartOfDay();
    }

    public static LocalDateTime getEndOfCurrentMonth() {
        return YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
    }

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

    public static long getFirstHalfOfMonthStart(LocalDate date) {
        LocalDateTime startOfFirstHalf = date.withDayOfMonth(1).atStartOfDay();
        return toUnixTimestamp(startOfFirstHalf);
    }

    public static long getFirstHalfOfMonthEnd(LocalDate date) {
        LocalDateTime endOfFirstHalf = date.withDayOfMonth(15).atTime(23, 59, 59);
        return toUnixTimestamp(endOfFirstHalf);
    }

    public static long getSecondHalfOfMonthStart(LocalDate date) {
        LocalDateTime startOfSecondHalf = date.withDayOfMonth(16).atStartOfDay();
        return toUnixTimestamp(startOfSecondHalf);
    }

    public static long getSecondHalfOfMonthEnd(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        LocalDateTime endOfSecondHalf = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return toUnixTimestamp(endOfSecondHalf);
    }

    private static long toUnixTimestamp(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}