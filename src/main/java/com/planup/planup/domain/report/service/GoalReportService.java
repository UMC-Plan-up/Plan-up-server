package com.planup.planup.domain.report.service;

import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.report.dto.GoalReportResponseDTO;
import com.planup.planup.domain.report.entity.GoalReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public interface GoalReportService {

    void createGoalReportsByUserGoal(LocalDateTime startDate, LocalDateTime endDate);

    List<GoalReport> findByGoalIdRecent2(Long id);
    GoalReportResponseDTO.GoalReportResponse findDTOById(Long id);

    @Transactional(readOnly = true)
    List<GoalReport> getListByUserIdOneDay(Long userId, LocalDateTime start, LocalDateTime end);

    @Transactional
    void createGoalReport(UserGoal userGoal, LocalDateTime startDate);

    @Transactional
    void createReportUsersFromRedis(UserGoal userGoal, LocalDateTime startDate);
}
