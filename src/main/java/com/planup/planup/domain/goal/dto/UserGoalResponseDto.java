package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.user.dto.UserDailySummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

public class UserGoalResponseDto {

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalTotalAchievementDto {
        @Schema(description = "목표 ID", example = "1")
        private Long goalId;

        @Schema(description = "전체 달성률 (퍼센트)", example = "85")
        private int totalAchievementRate;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimerGoalAchievementWithFriendDto {

        @Schema(description = "날짜")
        LocalDate date;

        @Schema(description = "총 시간을 초 단위로 변환해서 전송", example = "1800")
        long totalSeconds;

        @Schema(description = "사진기 아이콘에 들어갈 사진")
        String photoUrl;

        @Schema(description = "목표 이름", example = "토익 공부")
        String goalName;

        @Schema(description = "탈셩 여부", example = "true")
        boolean isAchievement;

        @Schema(description = "오늘의 메모", example = "힘들다")
        String todayMemo;

        @Schema(description = "빈도", example = "3")
        private int frequency;

        @Schema(description = "세부 기준", example = "하루에 3번 이상 공부하기")
        String goalAmount;

        @Schema(description = "친구에 대한 정보", example = "")
        List<UserDailySummaryDTO> friendInfoList;
    }
}
