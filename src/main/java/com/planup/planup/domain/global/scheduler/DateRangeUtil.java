package com.planup.planup.domain.global.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

public class DateRangeUtil {

    public static LocalDateTime getStartOfLastWeek() {
        return LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))  // 이번 주 월요일
                .minusWeeks(1) // 지난 주로 이동
                .atStartOfDay(); // 00:00:00
    }

    public static LocalDateTime getEndOfLastWeek() {
        return LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) // 이번 주 월요일
                .atStartOfDay() // 월요일 00:00
                .minusNanos(1); // 일요일 23:59:59.999999999
    }
}
