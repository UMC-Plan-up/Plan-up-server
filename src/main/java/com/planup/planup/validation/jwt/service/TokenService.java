package com.planup.planup.validation.jwt.service;

import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.global.constants.RedisKeyConstants;
import com.planup.planup.validation.jwt.JwtUtil;
import com.planup.planup.validation.jwt.dto.TokenResponseDTO;
import com.planup.planup.validation.jwt.dto.TokenRefreshResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> stringRedisTemplate;

    /**
     * 토큰 생성 및 Redis 저장
     */
    @Transactional
    public TokenResponseDTO generateTokens(User user) {
        // 액세스 토큰 생성
        String accessToken = jwtUtil.generateToken(
            user.getEmail(), 
            user.getRole().toString(), 
            user.getId()
        );
        
        // 리프레시 토큰 생성
        String refreshToken = jwtUtil.generateRefreshToken(
            user.getEmail(), 
            user.getId()
        );
        
        // Redis에 리프레시 토큰 저장 (기존 토큰 덮어쓰기)
        String refreshTokenKey = RedisKeyConstants.getRefreshTokenKey(user.getId());
        stringRedisTemplate.opsForValue().set(
            refreshTokenKey, 
            refreshToken, 
            RedisKeyConstants.REFRESH_TOKEN_TTL, 
            TimeUnit.SECONDS
        );
        
        log.info("토큰 생성 완료 - 사용자: {}", user.getEmail());
        
        return TokenResponseDTO.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(RedisKeyConstants.ACCESS_TOKEN_TTL)
            .build();
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 갱신
     */
    @Transactional
    public TokenRefreshResponseDTO refreshAccessToken(String refreshToken) {
        try {
            // JWT 검증
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new RuntimeException("유효하지 않은 리프레시 토큰입니다");
            }
            
            // 사용자 정보 추출
            String email = jwtUtil.extractUsername(refreshToken);
            Long userId = jwtUtil.extractUserId(refreshToken);
            
            // Redis에서 리프레시 토큰 조회
            String refreshTokenKey = RedisKeyConstants.getRefreshTokenKey(userId);
            String storedToken = stringRedisTemplate.opsForValue().get(refreshTokenKey);
            
            if (storedToken == null || !refreshToken.equals(storedToken)) {
                throw new RuntimeException("토큰이 존재하지 않거나 일치하지 않습니다");
            }
            
            // User 정보 조회 (role 필요)
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
            
            // 새 액세스 토큰 생성
            String newAccessToken = jwtUtil.generateToken(
                email, 
                user.getRole().toString(), 
                userId
            );
            
            log.info("토큰 갱신 완료 - 사용자: {}", email);
            
            return TokenRefreshResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // 리프레시 토큰은 그대로 유지
                .expiresIn(RedisKeyConstants.ACCESS_TOKEN_TTL)
                .build();
                
        } catch (Exception e) {
            log.error("토큰 갱신 실패: {}", e.getMessage());
            throw new RuntimeException("토큰 갱신에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 로그아웃 (리프레시 토큰 삭제)
     */
    @Transactional
    public void logout(Long userId) {
        try {
            // Redis에서 리프레시 토큰 삭제
            String refreshTokenKey = RedisKeyConstants.getRefreshTokenKey(userId);
            stringRedisTemplate.delete(refreshTokenKey);
            
            log.info("로그아웃 완료 - 사용자 ID: {}", userId);
            
        } catch (Exception e) {
            log.error("로그아웃 실패: {}", e.getMessage());
            throw new RuntimeException("로그아웃에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 액세스 토큰을 블랙리스트에 추가
     */
    public void blacklistAccessToken(String accessToken) {
        try {
            String blacklistKey = RedisKeyConstants.getBlacklistTokenKey(accessToken);
            stringRedisTemplate.opsForValue().set(
                blacklistKey, 
                "true", 
                RedisKeyConstants.BLACKLIST_TOKEN_TTL, 
                TimeUnit.SECONDS
            );
            
            log.info("액세스 토큰 블랙리스트 추가 완료");
            
        } catch (Exception e) {
            log.error("액세스 토큰 블랙리스트 추가 실패: {}", e.getMessage());
        }
    }

    /**
     * 액세스 토큰이 블랙리스트에 있는지 확인
     */
    public boolean isTokenBlacklisted(String accessToken) {
        try {
            String blacklistKey = RedisKeyConstants.getBlacklistTokenKey(accessToken);
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(blacklistKey));
        } catch (Exception e) {
            log.error("블랙리스트 확인 실패: {}", e.getMessage());
            return false;
        }
    }
}
