package com.planup.planup.domain.report.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.report.dto.GoalReportResponseDTO;
import com.planup.planup.domain.report.service.GoalReportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/report")
public class GoalReportController {

    private final GoalReportService goalReportService;

    @Operation(summary = "주간 목표 리포트 조회", description = "주간 리포트 - 목표별 상세 기록 조회")
    @GetMapping("/goal/{goalReportId}")
    public ApiResponse<GoalReportResponseDTO.GoalReportResponse> getGoalReport(Long userId, @PathVariable Long goalReportId) {

        GoalReportResponseDTO.GoalReportResponse result = goalReportService.findByGoalId(goalReportId);
        return ApiResponse.onSuccess(result);
    }
}
