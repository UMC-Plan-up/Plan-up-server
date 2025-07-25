package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class GoalRequestDto {

    @Data
    public static class CreateGoalDto {
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
