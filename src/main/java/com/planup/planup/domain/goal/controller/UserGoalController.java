package com.planup.planup.domain.goal.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.goal.dto.CommunityResponseDto;
import com.planup.planup.domain.goal.repository.GoalRepository;
import com.planup.planup.domain.goal.service.GoalService;
import com.planup.planup.domain.goal.service.UserGoalService;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community")
public class UserGoalController {

    private final UserGoalService userGoalService;

    @PostMapping("/{goalId}/join")
    @Operation(summary = "목표 참가 API", description = "커뮤니티 또는 친구가 생성한 목표에 참가합니다.")
    public ApiResponse<CommunityResponseDto.JoinGoalResponseDto> joinGoal(
            @PathVariable Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        CommunityResponseDto.JoinGoalResponseDto result = userGoalService.joinGoal(userId, goalId);

        return ApiResponse.onSuccess(result);
    }
}
