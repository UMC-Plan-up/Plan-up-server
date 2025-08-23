package com.planup.planup.domain.global.service;

import com.planup.planup.domain.goal.entity.Challenge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Service
public class AchievementCalculationService {

    //요일별 퍼센트를 계산한다.
    public Map<LocalDate, Integer> calcAchievementByDay(Map<LocalDate, Integer> dailyCount, int oneDose) {
        Map<LocalDate, Integer> result = new HashMap<>();

        // 분모 방어
        if (oneDose <= 0 || dailyCount == null || dailyCount.isEmpty()) {
            return result; // 전부 비어있는 Map
        }

        for (Map.Entry<LocalDate, Integer> e : dailyCount.entrySet()) {
            LocalDate date = e.getKey();
            if (date == null) continue;

            int value = e.getValue() == null ? 0 : e.getValue();
            int achievement = toPercent(value, oneDose);

            result.put(date, achievement);
        }
        return result;
    }

//    public void calcChallengeAchievement(Map<LocalDate, Integer> dailyCount, Challenge challenge) {
//        Date endDate = challenge.getEndDate();
//    }



    private int toPercent(int numerator, int denominator) {
        int p = (int) Math.round((numerator * 100.0) / denominator);

        // 0~100 클램프
        return Math.max(0, Math.min(100, p));
    }
}
