package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Enum.*;
import com.planup.planup.domain.goal.entity.PhotoVerification;
import com.planup.planup.domain.goal.entity.TimerVerification;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;


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
        VerificationType verificationType;
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
        Integer goalTime;
        Long spentTimeMinutes;
        int frequency;
        String oneDose;
        String creatorNickname;
        String creatorProfileImg;
        Status myStatus;
        int participantCount;
        boolean isActive;
    }

    //목표 세부 조회 Dto
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyGoalDetailDto {
        private Long goalId;
        private String goalName;
        private String oneDose;
        private boolean isActive;
        private LocalTime todayTime;
        private List<Comment> commentList;
    }
}
