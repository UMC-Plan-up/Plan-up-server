package com.planup.planup.domain.goal.repository.service;

import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.dto.UserGoalResponseDto;

import java.time.LocalDate;

public interface UserGoalAggregationService {
    GoalResponseDto.DailyAchievementDto getDailyAchievement(Long userId, LocalDate targetDate);

    UserGoalResponseDto.GoalTotalAchievementDto getTotalAchievement(Long goalId, Long userId);

    UserGoalResponseDto.GoalTotalAchievementDto getFriendGoalTotalAchievement(Long userId, Long goalId, Long friendId);
}
