package com.planup.planup.domain.global.message;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DailyLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    
    // Redis 키 prefix
    private static final String DAILY_LIMIT_PREFIX = "encouragement:daily:";
    private static final String MESSAGE_PREFIX = "encouragement:message:";
    private static final int TTL_HOURS = 24; // 24시간 TTL

    public boolean hasReceivedToday(Long userId) {
        String key = DAILY_LIMIT_PREFIX + userId;
        String today = LocalDate.now().toString();
        String storedDate = redisTemplate.opsForValue().get(key);
        
        return today.equals(storedDate);
    }

    public void markAsReceived(Long userId, String message) {
        String dateKey = DAILY_LIMIT_PREFIX + userId;
        String messageKey = MESSAGE_PREFIX + userId;
        String today = LocalDate.now().toString();
        
        // 날짜와 메시지를 각각 저장 (24시간 TTL)
        redisTemplate.opsForValue().set(dateKey, today, TTL_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(messageKey, message, TTL_HOURS, TimeUnit.HOURS);
    }

    public String getTodayMessage(Long userId) {
        String messageKey = MESSAGE_PREFIX + userId;
        return redisTemplate.opsForValue().get(messageKey);
    }

    public void clearTodayData(Long userId) {
        String dateKey = DAILY_LIMIT_PREFIX + userId;
        String messageKey = MESSAGE_PREFIX + userId;
        
        // 날짜와 메시지 데이터 모두 삭제
        redisTemplate.delete(dateKey);
        redisTemplate.delete(messageKey);
    }
}
