package com.planup.planup.domain.verification.service;


import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.verification.entity.TimerVerification;
import com.planup.planup.domain.verification.repository.TimerVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service
@RequiredArgsConstructor
public class TimerVerificationReadService {

    private final TimerVerificationRepository timerVerificationRepository;

    @Transactional(readOnly = true)
    public List<TimerVerification> getTimerVerificationListByUserAndDateBetween(UserGoal userGoal, LocalDateTime start, LocalDateTime end) {
        return timerVerificationRepository.findAllByUserGoalAndCreatedAtBetweenOrderByCreatedAt(userGoal, start, end);
    }


    @Transactional(readOnly = true)
    public Map<LocalDate, Integer> calculateVerification(UserGoal userGoal, LocalDateTime startDate, LocalDateTime endDate) {
        List<TimerVerification> verifications = getTimerVerificationListByUserAndDateBetween(userGoal, startDate, endDate);

        Map<LocalDate, Integer> dailyCount = new HashMap<>();

        for (TimerVerification verification : verifications) {
            LocalDate date = verification.getCreatedAt().toLocalDate();

            int seconds = (int) (verification.getSpentTime() != null ? verification.getSpentTime().toMillis() / 1000.0 : 0.0);

            dailyCount.put(date, dailyCount.getOrDefault(date, 0) + seconds);
        }
        return dailyCount;
    }
}
