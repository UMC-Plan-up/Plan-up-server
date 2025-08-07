package com.planup.planup.domain.report.service;

import com.planup.planup.domain.report.dto.WeeklyReportResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface WeeklyReportService {

    WeeklyReportResponseDTO.achievementResponse getWeeklyGoalAchievements(Long userId);

    List<Integer> searchWeeklyReport(Long userId, int year, int week);

    WeeklyReportResponseDTO.WeeklyReportResponse getWeeklyReport(Long userId, int year, int month, int week);

    @Transactional
    void createWeeklyReport(Long userId, LocalDateTime startDate);
}
