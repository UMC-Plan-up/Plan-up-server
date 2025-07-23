package com.planup.planup.domain.report.service;

import com.planup.planup.domain.report.dto.WeeklyRepoortResponseDTO;

import java.util.List;

public interface WeeklyReportService {

    WeeklyRepoortResponseDTO.achievementResponse getWeeklyGoalAchievements(Long userId);

    List<Integer> searchWeeklyReport(Long userId, int year, int week);

    WeeklyRepoortResponseDTO.WeeklyReportResponse getWeeklyReport(Long userId, int year, int month, int week);
}
