package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import lombok.*;

import java.util.Date;


public class GoalResponseDto {

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalResultDto {
        Long goalId;
        String goalName;
        String goalAmount;
        GoalCategory goalCategory;
        GoalType goalType;
        String oneDose;
        Date endDate;
        Boolean isChallenge;
        String currentAmount;
        int limitFriendCount;
    }
}
