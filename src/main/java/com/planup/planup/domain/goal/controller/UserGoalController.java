package com.planup.planup.domain.goal.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.friend.service.FriendService;
import com.planup.planup.domain.goal.dto.CommunityResponseDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.dto.UserGoalResponseDto;
import com.planup.planup.domain.goal.repository.service.UserGoalAggregationService;
import com.planup.planup.domain.goal.repository.service.UserGoalService;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community")
public class UserGoalController {

    private final UserGoalService userGoalService;
    private final FriendService friendService;
    private final UserGoalAggregationService userGoalAggregationService;

    @PostMapping("/{goalId}/join")
    @Operation(summary = "목표 참가 API", description = "커뮤니티 또는 친구가 생성한 목표에 참가합니다.")
    public ApiResponse<CommunityResponseDto.JoinGoalResponseDto> joinGoal(
            @PathVariable Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        CommunityResponseDto.JoinGoalResponseDto result = userGoalService.joinGoal(userId, goalId);

        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/daily-achievement")
    @Operation(summary = "일별 목표 달성률 조회 API", description = "특정 날짜의 사용자 전체 목표 달성률을 조회합니다.")
    public ApiResponse<GoalResponseDto.DailyAchievementDto> getDailyAchievement(
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd 형식)", example = "2024-08-21")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate targetDate,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        GoalResponseDto.DailyAchievementDto result = userGoalAggregationService.getDailyAchievement(userId, targetDate);
        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/{goalId}/total-achievement")
    @Operation(summary = "목표 전체 달성률 조회 API", description = "특정 목표의 전체 달성률을 조회합니다.")
    public ApiResponse<UserGoalResponseDto.GoalTotalAchievementDto> getGoalTotalAchievement(
            @Parameter(description = "목표 ID", example = "1")
            @PathVariable Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        UserGoalResponseDto.GoalTotalAchievementDto result = userGoalAggregationService.getTotalAchievement(goalId, userId);
        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/friend/{friendId}/goal/{goalId}/total-achievement")
    @Operation(summary = "친구 목표 전체 달성률 조회 API", description = "친구의 특정 목표 전체 달성률을 조회합니다.")
    public ApiResponse<UserGoalResponseDto.GoalTotalAchievementDto> getFriendGoalTotalAchievement(
            @Parameter(description = "친구 ID", example = "2")
            @PathVariable Long friendId,
            @Parameter(description = "목표 ID", example = "1")
            @PathVariable Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        UserGoalResponseDto.GoalTotalAchievementDto result = userGoalAggregationService.getFriendGoalTotalAchievement(userId, goalId, friendId);

        return ApiResponse.onSuccess(result);
    }
}
