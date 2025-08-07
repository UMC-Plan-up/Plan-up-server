package com.planup.planup.domain.verification.service;

import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.verification.entity.TimerVerification;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import com.planup.planup.domain.verification.repository.TimerVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class VerificationEntityFactory {

    private final TimerVerificationRepository timerVerificationRepository;
    private final PhotoVerificationRepository photoVerificationRepository;

    public Object createVerificationEntity(
            VerificationType type,
            UserGoal userGoal,
            GoalRequestDto.CreateGoalDto goalDto) {

        return switch (type) {
            case TIMER -> createTimerVerification(userGoal, goalDto);
            case PHOTO -> createPhotoVerification(userGoal);
            default -> throw new IllegalArgumentException("지원하지 않는 인증 타입: " + type);
        };
    }

    private TimerVerification createTimerVerification(UserGoal userGoal, GoalRequestDto.CreateGoalDto goalDto) {
        TimerVerification verification = TimerVerification.builder()
                .spentTime(Duration.ZERO)
                .userGoal(userGoal)
                .endTime(null)
                .build();

        return timerVerificationRepository.save(verification);
    }

    private PhotoVerification createPhotoVerification(UserGoal userGoal) {
        PhotoVerification verification = PhotoVerification.builder()
                .photoImg(null)
                .userGoal(userGoal)
                .build();

        return photoVerificationRepository.save(verification);
    }
}
