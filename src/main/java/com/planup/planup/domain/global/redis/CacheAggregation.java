package com.planup.planup.domain.global.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class CacheAggregation {

    private final RedisTemplate<String, String> redis;
    private final ObjectMapper objectMapper;


    public <T> T getOrCompute(
            String key,
            Duration ttl,
            Class<T> type,
            Supplier<T> loader
    ) {
        String raw = redis.opsForValue().get(key);
        if (raw != null) {
            try {
                return objectMapper.readValue(raw, type);
            } catch (Exception e) {
                // 파싱 실패시 캐시 무시하고 재계산
            }
        }
        //계산 값을 가져온다.
        T value = loader.get();

        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception ignore) {}

        return value;
    }

    public static Duration ttlUntilMidnight() {
        var now = java.time.LocalDateTime.now();
        var midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return java.time.Duration.between(now, midnight);
    }
}
