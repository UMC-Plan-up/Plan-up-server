package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.GoalPeriod;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import lombok.*;

import java.util.Date;

public class GoalRequestDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGoalDto {
        String goalName;
        String goalAmount;
        GoalCategory goalCategory;
        GoalType goalType;
        int oneDose;
        int frequency;
        GoalPeriod period;
        Date endDate;
        VerificationType verificationType;
        int limitFriendCount;
        Integer goalTime;
    }
}
