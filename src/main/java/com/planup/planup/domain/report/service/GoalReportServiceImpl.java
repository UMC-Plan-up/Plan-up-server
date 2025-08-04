package com.planup.planup.domain.report.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.ReportException;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.service.UserGoalService;
import com.planup.planup.domain.report.converter.GoalReportConverter;
import com.planup.planup.domain.report.dto.GoalReportResponseDTO;
import com.planup.planup.domain.report.entity.DailyAchievementRate;
import com.planup.planup.domain.report.entity.GoalReport;
import com.planup.planup.domain.report.entity.ThreeWeekAchievementRate;
import com.planup.planup.domain.report.repository.GoalReportRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.verification.entity.PhotoVerification;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class GoalReportServiceImpl implements GoalReportService {

    private final GoalReportRepository goalReportRepository;
    private final UserGoalService userGoalService;

    @Override
    public GoalReportResponseDTO.GoalReportResponse findByGoalId(Long id) {
        GoalReport goalReport = goalReportRepository.findById(id).orElseThrow(() -> new ReportException(ErrorStatus.NOT_FOUND_GOAL_REPORT));

        return GoalReportConverter.toResponse(goalReport);
    }

    public void createGoalReport(UserGoal userGoal, LocalDateTime startDate) {
        User user = userGoal.getUser();

        LocalDateTime endDate = startDate.plusDays(6);

        if (userGoal.getV)
    }

    private void calculatePhotoVerif(List<PhotoVerification> photoVerificationList, Goal goal) {
        int time = 0;
        int thisWeekRate;
        new DailyAchievementRate();

        for (PhotoVerification photoVerification : photoVerificationList) {

        }
    }
}
