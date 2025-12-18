package com.planup.planup.domain.report.service.GoalReportService;

import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.report.entity.DailyAchievementRate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface GoalReportWriteService {
    void createGoalReportsByUserGoal(LocalDateTime startDate, LocalDateTime endDate);

    @Transactional
    void createGoalReport(UserGoal userGoal, LocalDateTime startDate);

    @Transactional
    void createReportUsersFromRedis(UserGoal userGoal, LocalDateTime startDate);

    @Transactional(readOnly = true)
    DailyAchievementRate calculateDailyAchievementRate(UserGoal userGoal, Goal goal, LocalDateTime startDate);
}
