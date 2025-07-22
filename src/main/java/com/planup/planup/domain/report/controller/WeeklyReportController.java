package com.planup.planup.domain.report.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.report.dto.WeeklyRepoortResponseDTO;
import com.planup.planup.domain.report.service.WeeklyReportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/report")
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    @Operation(summary = "목표 달성 기록 조회 페이지 데이터 생성", description = "목표 달성 페이지의 알림, 메시지, 뱃지 내용을 조회")
    @GetMapping("/reports")
    public ApiResponse<WeeklyRepoortResponseDTO.achievementResponse> getAchievementPage(Long userId) {
        WeeklyRepoortResponseDTO.achievementResponse weeklyReport = weeklyReportService.getWeeklyReport(userId);
        return ApiResponse.onSuccess(weeklyReport);
    }

    @Operation(summary = "년/월을 기준으로 존재하는 리포트 리스트 반환",
            description = "목표 달성 페이지 접근 시, 년/월을 기준으로 주간 리포트를 검색하고 화면에 표시")
    @GetMapping("/reports/{year}/{month}")
    public ApiResponse<List<Integer>> searchExistWeeklyReportList(Long userId, @PathVariable int year, @PathVariable int month) {
        List<Integer> weeks = weeklyReportService.searchWeeklyReport(userId, year, month);
        return ApiResponse.onSuccess(weeks);
    }

    @Operation(summary = "주간 리포트 조회", description = "주간 리포트를 찾아 반환한다.")
    @GetMapping("/reports/{year}/{month}/{week}")
    public ApiResponse<WeeklyRepoortResponseDTO.weeklyReport> searchWeeklyReport(Long userId, @PathVariable int year, int month, int week) {
        week
    }
}
