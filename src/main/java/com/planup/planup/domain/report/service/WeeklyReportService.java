package com.planup.planup.domain.report.service;

import com.planup.planup.domain.report.dto.WeeklyRepoortResponseDTO;

import java.util.List;

public interface WeeklyReportService {
    WeeklyRepoortResponseDTO.achievementResponse getWeeklyReport(Long userId);

    List<Integer> searchWeeklyReport(int year, int week);
}
