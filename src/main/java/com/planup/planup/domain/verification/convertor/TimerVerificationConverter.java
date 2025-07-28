package com.planup.planup.domain.verification.convertor;

import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.verification.dto.TimerVerificationResponseDto;
import com.planup.planup.domain.verification.entity.TimerVerification;

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

}
