package com.planup.planup.domain.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisServiceForReport {

    private final RedisTemplate<String, String> redisTemplate;

    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    public void saveUserValue(String userId, String goalId, int value) {
        String key = generateKey(userId, goalId, "value");
        redisTemplate.opsForValue().set(key, String.valueOf(value), DEFAULT_TTL);
    }

    public Integer getUserValue(String userId, String goalId) {
        String key = generateKey(userId, goalId, "value");
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : null;
    }

    public void saveUserReport(String userId, String goalId, Long reportId) {
        String key = generateKey(userId, goalId, "reportId");
        redisTemplate.opsForValue().set(key, String.valueOf(reportId), DEFAULT_TTL);
    }

    public Integer getUserReport(String userId, String goalId) {
        String key = generateKey(userId, goalId, "reportId");
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : null;
    }

    private String generateKey(String userId, String goalId, String suffix) {
        return "user:" + userId + ":goal:" + goalId + ":" + suffix;
    }
}
