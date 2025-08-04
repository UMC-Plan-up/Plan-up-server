package com.planup.planup.domain.user.verification.convertor;

import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.verification.dto.TimerVerificationResponseDto;
import com.planup.planup.domain.user.verification.entity.TimerVerification;

import java.time.LocalTime;

public class TimerVerificationConverter {

    public static TimerVerificationResponseDto.TimerStartResponseDto toTimerStartResponse(
            TimerVerification timerVerification) {

        UserGoal userGoal = timerVerification.getUserGoal();

        return TimerVerificationResponseDto.TimerStartResponseDto.builder()
                .timerId(timerVerification.getId())
                .userGoalId(userGoal.getId())
                .goalTimeMinutes(userGoal.getGoalTime())
                .startTime(timerVerification.getCreatedAt())
                .build();
    }

    public static TimerVerificationResponseDto.TimerStopResponseDto toTimerStopResponse(
            TimerVerification timerVerification, boolean isGoalAchieved) {

        UserGoal userGoal = timerVerification.getUserGoal();

        return TimerVerificationResponseDto.TimerStopResponseDto.builder()
                .timerId(timerVerification.getId())
                .userGoalId(userGoal.getId())
                .totalSpentTime(timerVerification.getSpentTime())
                .goalTimeMinutes(userGoal.getGoalTime())
                .isGoalAchieved(isGoalAchieved)
                .endTime(timerVerification.getEndTime())
                .startTime(timerVerification.getCreatedAt())
                .currentVerificationCount(userGoal.getVerificationCount())
                .build();
    }

    public static TimerVerificationResponseDto.TodayTotalTimeResponseDto toTodayTotalTimeResponse(
            LocalTime totalTime) {

        String formattedTime = String.format("%02d:%02d:%02d",
                totalTime.getHour(),
                totalTime.getMinute(),
                totalTime.getSecond());

        return TimerVerificationResponseDto.TodayTotalTimeResponseDto.builder()
                .formattedTime(formattedTime)
                .build();
    }
}
