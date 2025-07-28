package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Enum.*;
import lombok.*;

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
        private Long goalId;
        private String goalName;
        private GoalCategory goalCategory;
        private GoalType goalType;
        private VerificationType verificationType;
        private Integer goalTime;
        private Long spentTimeMinutes;
        private int frequency;
        private String oneDose;
        private String creatorNickname;
        private String creatorProfileImg;
        private Status myStatus;
        private int participantCount;
        private boolean isActive;
        private boolean isPublic;
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
        private boolean isPublic;
        private LocalTime todayTime;
        private List<Comment> commentList;
    }
}
