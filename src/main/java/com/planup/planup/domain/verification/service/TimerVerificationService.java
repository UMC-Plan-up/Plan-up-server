package com.planup.planup.domain.verification.service;

import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.verification.convertor.TimerVerificationConverter;
import com.planup.planup.domain.verification.dto.TimerVerificationResponseDto;
import com.planup.planup.domain.verification.entity.TimerVerification;
import com.planup.planup.domain.verification.repository.TimerVerificationRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TimerVerificationService implements VerificationService{
    private final UserGoalRepository userGoalRepository;
    private final TimerVerificationRepository timerVerificationRepository;

    public LocalTime getTodayTotalTime(Long userGoalId) {
        List<TimerVerification> todayVerifications = timerVerificationRepository
                .findTodayVerificationsByUserGoalId(userGoalId);

        if (todayVerifications.isEmpty()) {
            return LocalTime.of(0, 0, 0);
        }

        Duration total = todayVerifications.stream()
                .filter(tv -> tv.getSpentTime() != null)
                .map(TimerVerification::getSpentTime)
                .reduce(Duration.ZERO, Duration::plus);

        return LocalTime.of(
                (int) total.toHours(),
                (int) (total.toMinutes() % 60),
                (int) (total.getSeconds() % 60)
        );
    }

    public TimerVerificationResponseDto.TimerStartResponseDto startTimer(Long userId, Long goalId) {
        UserGoal userGoal = userGoalRepository.findByGoalIdAndUserId(goalId, userId);
        //에러 처리 필요

        List<TimerVerification> runningTimers = timerVerificationRepository
                .findByUserGoal_User_IdAndEndTimeIsNull(userId);

        if (!runningTimers.isEmpty()) {
            String runningGoalName = runningTimers.get(0).getUserGoal().getGoal().getGoalName();
            throw new RuntimeException("이미 진행중인 타이머가 있습니다: " + runningGoalName);
        }

        TimerVerification timer = TimerVerification.builder()
                .userGoal(userGoal)
                .spentTime(Duration.ZERO)
                .endTime(null)
                .build();

        TimerVerification savedTimer = timerVerificationRepository.save(timer);

        return TimerVerificationConverter.toTimerStartResponse(savedTimer);
    }

}
