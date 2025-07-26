package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;

import java.util.List;

public interface GoalService {
    //Query Service

    //Command Service
    GoalResponseDto.GoalResultDto createGoal(Long userId, GoalRequestDto.CreateGoalDto dto);
    List<GoalResponseDto.MyGoalListDto> getMyGoals(Long userId);
    GoalResponseDto.MyGoalDetailDto getMyGoalDetails(Long goalId, Long userId);
    GoalResponseDto.MyGoalDetailDto updateActiveGoal(Long goalId, Long userId);
}
