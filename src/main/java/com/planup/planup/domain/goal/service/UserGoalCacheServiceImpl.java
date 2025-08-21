package com.planup.planup.domain.goal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserGoalCacheServiceImpl implements UserGoalCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    private Duration getDuration() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, midnight);
    }

    @Override
    public void saveUserDailyAchievement(String userId, int rate) {
        String key = generateKey(userId, "DailyAch");
        redisTemplate.opsForValue().set(key, String.valueOf(rate), getDuration());
    }

    @Override
    public Integer getDailyAchievement(String userId) {
        String key = generateKey(userId, "DailyAch");
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Integer.parseInt(val) : null;
    }

    @Override
    public void saveTotalAchievementGoal(String userId, String goalId, int rate) {
        String key = generateKeyUserAndGoal(userId, goalId, "goalAchievement");
        redisTemplate.opsForValue().set(key, String.valueOf(rate), getDuration());
    }

    @Override
    public Integer getTotalAchievementGoal(String userId, String goalId) {
        String key = generateKeyUserAndGoal(userId, goalId, "goalAchievement");
        String val = redisTemplate.opsForValue().get(key);
        return val != null ? Integer.parseInt(val) : null;
    }


    private String generateKey(String userId, String suffix) {
        return "user:" + userId + ":" + suffix;
    }

    private String generateKeyUserAndGoal(String userId, String goalId, String suffix) {
        return "user:" + userId + ":goal:" + goalId + ":" + suffix;
    }
}
