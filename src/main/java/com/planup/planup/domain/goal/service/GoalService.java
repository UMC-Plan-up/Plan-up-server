package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.entity.Goal;

public interface GoalService {
    GoalResponseDto.GoalResultDto createGoal(Long userId, GoalRequestDto.CreateGoalDto dto);

    Goal getGoalById(Long id);
}
