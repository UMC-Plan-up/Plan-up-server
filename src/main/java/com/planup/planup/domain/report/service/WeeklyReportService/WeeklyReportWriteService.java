package com.planup.planup.domain.report.service.WeeklyReportService;

import com.planup.planup.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface WeeklyReportWriteService {
    void createWeeklyReportsByUserGoal(LocalDateTime startDate, LocalDateTime endDate);

    @Transactional
    void createWeeklyReport(User user, LocalDateTime startDate);
}
