package com.planup.planup.domain.report.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.report.dto.WeeklyReportResponseDTO;
import com.planup.planup.domain.report.service.WeeklyReportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/report")
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    @Operation(summary = "목표 달성 기록 조회 페이지 데이터 생성", description = "목표 달성 페이지의 알림, 메시지, 뱃지 내용을 조회")
    @GetMapping("/reports")
    public ApiResponse<WeeklyReportResponseDTO.achievementResponse> getAchievementPage(Long userId) {
        WeeklyReportResponseDTO.achievementResponse weeklyReport = weeklyReportService.getWeeklyGoalAchievements(userId);
        return ApiResponse.onSuccess(weeklyReport);
    }

    @Operation(summary = "년/월을 기준으로 존재하는 리포트 리스트 반환",
            description = "목표 달성 페이지 접근 시, 년/월을 기준으로 주간 리포트를 검색하고 화면에 표시")
    @GetMapping("/reports/{year}/{month}")
    public ApiResponse<List<Integer>> searchExistWeeklyReportList(Long userId, @PathVariable int year, @PathVariable int month) {
        List<Integer> weeks = weeklyReportService.searchWeeklyReport(userId, year, month);
        return ApiResponse.onSuccess(weeks);
    }

    @Operation(summary = "각 주차별 종합 리포트 반환", description = "목표 달성 페이지에서 주차별 리포트를 선택하면 해당 주차 리포트를 반환한다.")
    @GetMapping("/reports/{year}/{month}/{week}")
    public ApiResponse<WeeklyReportResponseDTO.WeeklyReportResponse> getWeeklyReport(Long userId, int year, int month, int week) {
        WeeklyReportResponseDTO.WeeklyReportResponse weeklyReport = weeklyReportService.getWeeklyReport(userId, year, month, week);
        return ApiResponse.onSuccess(weeklyReport);
    }
}
