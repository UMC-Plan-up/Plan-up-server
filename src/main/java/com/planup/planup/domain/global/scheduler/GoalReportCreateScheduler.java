package com.planup.planup.domain.global.scheduler;

import com.planup.planup.domain.report.service.GoalReportService.GoalReportWriteService;
import com.planup.planup.domain.report.service.WeeklyReportService.WeeklyReportWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoalReportCreateScheduler {

    private final GoalReportWriteService goalReportWriteService;
    private final WeeklyReportWriteService weeklyReportWriteService;
    @Scheduled(cron = "0 0 0 * * MON", zone = "Asia/Seoul")
    public void publishReport() {
        goalReportWriteService.createGoalReportsByUserGoal(DateRangeUtil.getStartOfLastWeek(), DateRangeUtil.getEndOfLastWeek());

        weeklyReportWriteService.createWeeklyReportsByUserGoal(DateRangeUtil.getStartOfLastWeek(), DateRangeUtil.getEndOfLastWeek());
    }
}
