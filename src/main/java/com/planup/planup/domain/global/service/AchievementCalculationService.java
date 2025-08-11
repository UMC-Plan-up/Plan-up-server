package com.planup.planup.domain.global.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

@Service
public class AchievementCalculationService {

    //요일별 퍼센트를 계산한다.
    public Map<DayOfWeek, Integer> calcAchievementByDay(Map<LocalDate, Integer> dailyCount, int oneDose) {
        Map<DayOfWeek, Integer> result = new EnumMap<>(DayOfWeek.class);

        // 1) 기본값 0으로 7일 채우기
        for (DayOfWeek d : DayOfWeek.values()) {
            result.put(d, 0);
        }
        // 2) 분모 방어
        if (oneDose <= 0 || dailyCount == null || dailyCount.isEmpty()) {
            return result; // 전부 0%
        }

        // 3) 각 요일에 대해 계산
        for (Map.Entry<LocalDate, Integer> e : dailyCount.entrySet()) {
            DayOfWeek dow = e.getKey().getDayOfWeek();
            int value = e.getValue() == null ? 0 : e.getValue();

            //퍼센트로 바꾼다.
            int achievement = toPercent(value, oneDose);

            result.put(dow, achievement);
        }
        return result;
    }

    private int toPercent(int numerator, int denominator) {
        // 정책에 따라 내림/올림/반올림 선택
        // 반올림:
        int p = (int) Math.round((numerator * 100.0) / denominator);
        // 내림을 원하면:
        // int p = (int) Math.floor((numerator * 100.0) / denominator);

        // 0~100 클램프
        return Math.max(0, Math.min(100, p));
    }
}
