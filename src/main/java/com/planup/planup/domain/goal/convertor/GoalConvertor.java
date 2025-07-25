package com.planup.planup.domain.goal.convertor;

import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.entity.Goal;

public class GoalConvertor {

    //목표 생성 Dto를 받아 goal 엔터티에 저장하도록 변환
    public static Goal toGoal(GoalRequestDto.CreateGoalDto createGoalDto) {
        return Goal.builder()
                .goalName(createGoalDto.getGoalName())
                .goalAmount(createGoalDto.getGoalAmount())
                .goalCategory(createGoalDto.getGoalCategory())
                .goalType(createGoalDto.getGoalType())
                .oneDose(createGoalDto.getOneDose())
                .endDate(createGoalDto.getEndDate())
                .isChallenge(createGoalDto.getIsChallenge())
                .limitFriendCount(createGoalDto.getLimitFriendCount())
                .build();
    }

    public static GoalResponseDto.GoalResultDto toGoalResultDto(Goal goal) {
        return GoalResponseDto.GoalResultDto.builder()
                .goalId(goal.getId())
                .goalName(goal.getGoalName())
                .goalAmount(goal.getGoalAmount())
                .goalCategory(goal.getGoalCategory())
                .goalType(goal.getGoalType())
                .oneDose(goal.getOneDose())
                .endDate(goal.getEndDate())
                .isChallenge(goal.getIsChallenge())
                .limitFriendCount(goal.getLimitFriendCount())
                .build();
    }
}
