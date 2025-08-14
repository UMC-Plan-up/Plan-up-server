package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Enum.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
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

    //친구 타이머 현황 Dto
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FriendTimerStatusDto {
        private Long userId;
        private String nickname;
        private String profileImg;
        private String todayTime;
        private VerificationType verificationType;
    }

    //메모 Dto
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalMemoResponseDto {
        @Schema(description = "처리 결과", example = "CREATED", allowableValues = {"CREATED", "UPDATED", "DELETED", "NO_CHANGE"})
        private String action;

        @Schema(description = "메모 ID (삭제된 경우 null)", example = "1")
        private Long memoId;

        @Schema(description = "메모 내용 (삭제된 경우 null)", example = "오늘의 메모")
        private String memo;

        private LocalDate memoDate;

        @Schema(description = "처리 결과 메시지", example = "메모가 성공적으로 저장되었습니다.")
        private String message;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalMemoReadDto {
        private String memo;
        private LocalDate memoDate;
        private boolean exists;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyVerifiedGoalsResponse {
        private LocalDate date;
        private List<VerifiedGoalInfo> verifiedGoals;
        private int totalCount;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifiedGoalInfo {
        private String goalName;
        private GoalPeriod period;
        private int frequency;
    }
}
