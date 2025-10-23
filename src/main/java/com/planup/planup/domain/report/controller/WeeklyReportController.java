package com.planup.planup.domain.report.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.goal.dto.CommentRequestDto;
import com.planup.planup.domain.goal.dto.CommentResponseDto;
import com.planup.planup.domain.goal.service.CommentService;
import com.planup.planup.domain.report.dto.WeeklyReportResponseDTO;
import com.planup.planup.domain.report.service.WeeklyReportService.WeeklyReportReadService;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/report")
public class WeeklyReportController {

    private final WeeklyReportReadService weeklyReportReadService;
    private final CommentService commentService;

    @Operation(summary = "목표 달성 기록 조회 페이지 데이터 생성", description = "목표 달성 페이지의 알림, 메시지, 뱃지 내용을 조회")
    @GetMapping("/reports")
    public ApiResponse<WeeklyReportResponseDTO.achievementResponse> getAchievementPage(@Parameter(hidden = true) @CurrentUser Long userId) {
        WeeklyReportResponseDTO.achievementResponse weeklyReport = weeklyReportReadService.getWeeklyGoalAchievements(userId);
        return ApiResponse.onSuccess(weeklyReport);
    }

    @Operation(summary = "년/월을 기준으로 존재하는 리포트 리스트 반환",
            description = "목표 달성 페이지 접근 시, 년/월을 기준으로 주간 리포트를 검색하고 화면에 표시")
    @GetMapping("/reports/{year}/{month}")
    public ApiResponse<List<Integer>> searchExistWeeklyReportList(Long userId, @PathVariable int year, @PathVariable int month) {
        List<Integer> weeks = weeklyReportReadService.searchWeeklyReport(userId, year, month);
        return ApiResponse.onSuccess(weeks);
    }

    @Operation(summary = "각 주차별 종합 리포트 반환", description = "목표 달성 페이지에서 주차별 리포트를 선택하면 해당 주차 리포트를 반환한다.")
    @GetMapping("/reports/{year}/{month}/{week}")
    public ApiResponse<WeeklyReportResponseDTO.WeeklyReportResponse> getWeeklyReport(@Parameter(hidden = true) @CurrentUser Long userId,
                                                                                     @Parameter(description = "연도", example = "2025") @PathVariable int year,
                                                                                     @Parameter(description = "월(1-12)", example = "8") @PathVariable int month,
                                                                                     @Parameter(description = "주차(1-5)", example = "3") @PathVariable int week) {
        WeeklyReportResponseDTO.WeeklyReportResponse weeklyReport = weeklyReportReadService.getWeeklyReport(userId, year, month, week);
        return ApiResponse.onSuccess(weeklyReport);
    }

    @PostMapping("/{goalId}/comments")
    @Operation(summary = "댓글 작성 API", description = "특정 목표에 댓글을 작성합니다.")
    public ApiResponse<CommentResponseDto.CommentDto> createComment(
            @PathVariable Long goalId,
            @Valid @RequestBody CommentRequestDto.CommentCreateRequestDto requestDto,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        CommentResponseDto.CommentDto result = commentService.createCommentByGoal(goalId, userId, requestDto);
        return ApiResponse.onSuccess(result);
    }
}
