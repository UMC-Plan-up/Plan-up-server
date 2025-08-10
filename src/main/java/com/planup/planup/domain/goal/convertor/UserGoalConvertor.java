package com.planup.planup.domain.goal.convertor;

import com.planup.planup.domain.goal.dto.CommunityResponseDto;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.entity.User;
import lombok.Data;

public class UserGoalConvertor {
    public static CommunityResponseDto.JoinGoalResponseDto toJoinGoalResponseDto(
            UserGoal userGoal, Goal goal, User user) {

        return CommunityResponseDto.JoinGoalResponseDto.builder()
                .goalId(goal.getId())
                .goalTitle(goal.getGoalName())
                .userId(user.getId())
                .status(userGoal.getStatus())
                .goalType(goal.getGoalType())
                .build();
    }

}
