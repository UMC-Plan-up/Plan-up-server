package com.planup.planup.domain.user.service;

import com.planup.planup.domain.user.dto.InviteCodeResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteCodeService {

    private final RedisTemplate<String, String> redisTemplate;

    // 내 초대코드 조회/생성
    public InviteCodeResponseDTO getMyInviteCode(Long userId) {
        // 기존 코드 확인
        String key = "user:" + userId + ":invite_code";
        String existingCode = redisTemplate.opsForValue().get(key);

        if (existingCode != null) {
            return InviteCodeResponseDTO.of(existingCode);
        }

        // 새 코드 생성
        String newCode = generateCode();

        // Redis에 저장 (30분 TTL)
        redisTemplate.opsForValue().set(key, newCode, 30, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("invite_code:" + newCode, userId.toString(), 30, TimeUnit.MINUTES);

        return InviteCodeResponseDTO.of(newCode);
    }

    // 초대코드로 초대한 사용자 ID 찾기
    public Long findInviterByCode(String inviteCode) {
        String inviterIdStr = redisTemplate.opsForValue().get("invite_code:" + inviteCode);

        if (inviterIdStr != null) {
            return Long.parseLong(inviterIdStr);
        }

        return null;
    }

    // 초대코드 사용 완료 처리
    public void useInviteCode(String inviteCode) {
        redisTemplate.delete("invite_code:" + inviteCode);
    }

    // 6자리 랜덤 코드 생성
    private String generateCode() {
        Random random = new Random();
        String code = String.format("%06d", random.nextInt(1000000));

        // 중복 확인
        while (Boolean.TRUE.equals(redisTemplate.hasKey("invite_code:" + code))) {
            code = String.format("%06d", random.nextInt(1000000));
        }

        return code;
    }
}