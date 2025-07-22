package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.goal.entity.Enum.*;
import lombok.*;

import java.time.LocalDateTime;
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
        int frequency;
        GoalPeriod period;
        Date endDate;
        Boolean isChallenge;
        int limitFriendCount;
    }

    //목표 조회 리스트 Dto
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyGoalListDto {
        Long goalId;
        String goalName;
        GoalCategory goalCategory;
        GoalType goalType;
        VerificationType verificationType;
        int frequency;
        String oneDose;
        String currentAmount;
        String creatorNickname;
        String creatorProfileImg;
        Status myStatus;
        int participantCount;
        Boolean isActive;
    }

    //목표 세부 조회 Dto
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyGoalDto {
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
        Status myStatus;
        Boolean isActive;
        VerificationType verificationType;
        int participantCount;
        LocalDateTime createdAt;
    }
}
