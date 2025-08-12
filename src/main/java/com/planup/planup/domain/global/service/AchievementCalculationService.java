package com.planup.planup.domain.global.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

@Service
public class AchievementCalculationService {

    @Transactional(readOnly = true)
    //요일별 퍼센트를 계산한다.
    public Map<DayOfWeek, Integer> calcAchievementByDay(Map<LocalDate, Integer> dailyCount, int oneDose) {
        Map<DayOfWeek, Integer> result = new EnumMap<>(DayOfWeek.class);
        if (oneDose <= 0) return result; // 방어코드

        for (Map.Entry<LocalDate, Integer> e : dailyCount.entrySet()) {
            DayOfWeek dow = e.getKey().getDayOfWeek();
            int totalPhotoCount = e.getValue() == null ? 0 : e.getValue();

            // 백분율 계산 (내림), 최대 100
            int achievement = (int) Math.min(100, ((double) totalPhotoCount / oneDose) * 100);

            result.put(dow, achievement);
        }
        return result;
    }
}
