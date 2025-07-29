package com.planup.planup.domain.goal.service.verification;

import com.planup.planup.domain.goal.entity.TimerVerification;
import com.planup.planup.domain.goal.repository.TimerVerificationRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimerVerificationService implements VerificationService{
    private final UserGoalRepository userGoalRepository;
    private final TimerVerificationRepository timerVerificationRepository;

    public LocalTime getTodayTotalTime(Long userGoalId) {
        List<TimerVerification> todayVerifications = timerVerificationRepository.findTodayVerificationsByUserGoalId(userGoalId);

        if (todayVerifications.isEmpty()) {
            return LocalTime.of(0, 0, 0);
        } else {
            Duration total = todayVerifications.stream()
                    .map(TimerVerification::getSpentTime)
                    .reduce(Duration.ZERO, Duration::plus);

            return LocalTime.of(
                    (int) total.toHours(),
                    (int) (total.toMinutes() % 60),
                    (int) (total.getSeconds() % 60)
            );
        }
    }
}
