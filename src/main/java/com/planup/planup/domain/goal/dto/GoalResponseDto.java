package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Enum.*;
import lombok.*;

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
        int oneDose;
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
    public static class GoalCreateListDto {
        private Long goalId;
        private String goalName;
        private GoalCategory goalCategory;
        private GoalType goalType;
        private VerificationType verificationType;
        private Integer goalTime;
        private int frequency;
        private int oneDose;
        private String creatorNickname;
        private String creatorProfileImg;
        private int participantCount;
    }

    //내 목표 리스트 조회 DTO
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyGoalListDto {
        private Long goalId;
        private String goalName;
        private GoalType goalType;
        private int frequency;
        private int oneDose;
    }

    //친구 목표 조회 리스트 Dto
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FriendGoalListDto {
        private Long goalId;
        private String goalName;
        private GoalType goalType;
        private VerificationType verificationType;
        private Integer goalTime;
        private int frequency;
        private int oneDose;
    }

    //목표 세부 조회 Dto
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyGoalDetailDto {
        private Long goalId;
        private String goalName;
        private int oneDose;
        private boolean isPublic;
        private List<Comment> commentList;
    }

    //랭킹 Dto
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankingDto {
        private Long goalId;
        private Long userId;
        private String nickName;
        private String profileImg;
        private int verificationCount;
    }
}
