package com.planup.planup.domain.report.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.report.dto.GoalReportResponseDTO;
import com.planup.planup.domain.report.dto.WeeklyRepoortResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/report")
public class GoalReportController {

    @Operation(summary = "주간 리포트 조회", description = "주간 리포트를 찾아 반환한다.")
    @GetMapping("/reports/{year}/{month}/{week}")
    public ApiResponse<GoalReportResponseDTO.GoalReportResponse> searchWeeklyReport(Long userId, @PathVariable int year, int month, int week) {

    }
}
