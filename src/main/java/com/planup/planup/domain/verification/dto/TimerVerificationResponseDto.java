package com.planup.planup.domain.user.verification.dto;

import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimerVerificationResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor
    public static class TimerStartResponseDto {
        private Long timerId;
        private Long userGoalId;
        private Integer goalTimeMinutes;
        private LocalDateTime startTime;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimerStopResponseDto {
        private Long timerId;
        private Long userGoalId;
        private Duration totalSpentTime;
        private int goalTimeMinutes;
        private boolean isGoalAchieved;
        private LocalDateTime endTime;
        private LocalDateTime startTime;
        private int currentVerificationCount;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodayTotalTimeResponseDto {

        private String formattedTime;
    }
}
