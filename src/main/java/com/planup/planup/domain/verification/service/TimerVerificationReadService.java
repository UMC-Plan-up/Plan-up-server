package com.planup.planup.domain.verification.service;


import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.verification.entity.TimerVerification;
import com.planup.planup.domain.verification.repository.TimerVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public List<TimerVerification> getTimerVerificationListByUserGoal(UserGoal userGoal) {
        return timerVerificationRepository.findAllByUserGoal(userGoal);
    }


    @Transactional(readOnly = true)
    public Map<LocalDate, Integer> calculateVerification(UserGoal userGoal, LocalDateTime startDate, LocalDateTime endDate) {
        List<TimerVerification> verifications = getTimerVerificationListByUserAndDateBetween(userGoal, startDate, endDate);

        return calcVerificationLocalDate(verifications);
    }

    private static Map<LocalDate, Integer> calcVerificationLocalDate(List<TimerVerification> verifications) {
        Map<LocalDate, Integer> dailyCount = new HashMap<>();

        for (TimerVerification verification : verifications) {
            LocalDate date = verification.getCreatedAt().toLocalDate();

            int seconds = (int) (verification.getSpentTime() != null ? verification.getSpentTime().toMillis() / 1000.0 : 0.0);

            dailyCount.put(date, dailyCount.getOrDefault(date, 0) + seconds);
        }
        return dailyCount;
    }

    @Transactional
    public Map<LocalDate, Integer> calculateVerificationWithGoal(UserGoal userGoal) {
        List<TimerVerification> verifications = getTimerVerificationListByUserGoal(userGoal);

        return calcVerificationLocalDate(verifications);
    }

    //오늘 userGoal 별 총 기록시간 조회
    public LocalTime getTodayTotalTimeByUserGoal(UserGoal userGoal) {
        if (userGoal == null) {
            return LocalTime.of(0, 0, 0);
        }


        //리포지토리에서 조건에 맞는 값들을 찾아서 다 더해 반환한다.
        Integer spendTimeInSeconds = timerVerificationRepository.sumTodayVerificationsByUserGoalId(userGoal.getId());

        //예외처리
        if (spendTimeInSeconds == null) {
            spendTimeInSeconds = 0;
        }

        return LocalTime.ofSecondOfDay(spendTimeInSeconds);
    }
}
