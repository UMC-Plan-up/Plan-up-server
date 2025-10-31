package com.planup.planup.domain.report.service.GoalReportService;

import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.report.dto.GoalReportResponseDTO;
import com.planup.planup.domain.report.entity.DailyAchievementRate;
import com.planup.planup.domain.report.entity.GoalReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public interface GoalReportService {

    void createGoalReportsByUserGoal(LocalDateTime startDate, LocalDateTime endDate);

    List<GoalReport> findTop2RecentByGoalId(Long id);

    @Transactional(readOnly = true)
    GoalReport getGoalReportOrThrow(Long id);

    GoalReportResponseDTO.GoalReportResponse findDTOById(Long id, Long userId);

    @Transactional(readOnly = true)
    List<GoalReport> getListByUserIdOneDay(Long userId, LocalDateTime start, LocalDateTime end);

    @Transactional
    void createGoalReport(UserGoal userGoal, LocalDateTime startDate);

    @Transactional
    void createReportUsersFromRedis(UserGoal userGoal, LocalDateTime startDate);

    //각 인증을 취합하여 DailyAchievementRate를 생성한다.
    DailyAchievementRate calculateDailyAchievementRate(UserGoal userGoal, Goal goal, LocalDateTime startDate);

    public List<GoalReport> getGoalReportsByUserAndPeriod(Long userId, LocalDateTime start, LocalDateTime end);
}
