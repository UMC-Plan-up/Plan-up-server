package com.planup.planup.domain.report.service;

import com.planup.planup.domain.report.dto.WeeklyReportResponseDTO;

import java.util.List;

public interface WeeklyReportService {

    WeeklyReportResponseDTO.achievementResponse getWeeklyGoalAchievements(Long userId);

    List<Integer> searchWeeklyReport(Long userId, int year, int week);

    WeeklyReportResponseDTO.WeeklyReportResponse getWeeklyReport(Long userId, int year, int month, int week);
}
