package com.planup.planup.domain.report.service.WeeklyReportService;

import com.planup.planup.domain.report.dto.WeeklyReportResponseDTO;

import java.util.List;

public interface WeeklyReportReadService {
    List<Integer> searchWeeklyReport(Long userId, int year, int month);

    WeeklyReportResponseDTO.achievementResponse getWeeklyGoalAchievements(Long userId);

    WeeklyReportResponseDTO.WeeklyReportResponse getWeeklyReport(Long userId, int year, int month, int week);
}
