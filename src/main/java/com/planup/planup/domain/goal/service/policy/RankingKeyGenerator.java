package com.planup.planup.domain.goal.service.policy;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class RankingKeyGenerator {

    public static String goalAll(Long goalId) {
        return "ranking:goal:" + goalId + ":all";
    }

    public static String goalDaily(Long goalId, LocalDate date) {
        return "ranking:goal:" + goalId + ":daily:" + date;
    }

    public static String goalWeekly(Long goalId, LocalDate date) {
        WeekFields wf = WeekFields.of(Locale.KOREA);
        int week = date.get(wf.weekOfWeekBasedYear());
        int year = date.get(wf.weekBasedYear());
        return "ranking:goal:" + goalId + ":weekly:" + year + "-W" + week;
    }

    public static String goalMonthly(Long goalId, LocalDate date) {
        return "ranking:goal:" + goalId + ":monthly:" + date.getYear() + "-" + String.format("%02d", date.getMonthValue());
    }
}
