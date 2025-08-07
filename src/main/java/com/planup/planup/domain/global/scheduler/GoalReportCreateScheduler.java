package com.planup.planup.domain.global.scheduler;

import com.planup.planup.domain.report.service.GoalReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoalReportCreateScheduler {

    private final GoalReportService goalReportService;

    @Scheduled(cron = "0 0 0 * * MON", zone = "Asia/Seoul")
    public void publishReport() {
        goalReportService.createGoalReportsByUserGoal(DateRangeUtil.getStartOfLastWeek(), DateRangeUtil.getEndOfLastWeek());
    }
}
