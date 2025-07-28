package com.planup.planup.domain.verification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
}
