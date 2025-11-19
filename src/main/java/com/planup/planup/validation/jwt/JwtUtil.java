package com.planup.planup.validation.jwt;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.TokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Getter
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.accessTokenExpiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refreshTokenExpiration}")
    private Long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    //토큰 추출
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            }
            throw new TokenException(ErrorStatus.TOKEN_INVALID);
        });
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new TokenException(ErrorStatus.TOKEN_INVALID);
        }
    }

    //토큰 만료 여부체크
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    //Access Token 생성
    public String generateToken(String username, String role, Long userId) {
        // 입력값 검증
        if (username == null || role == null || userId == null) {
            throw new TokenException(ErrorStatus.TOKEN_INVALID_INPUT_VALUE);
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);

        return createToken(claims, username, accessTokenExpiration);
    }

    //Refresh Token
    public String generateRefreshToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return createToken(claims, username, refreshTokenExpiration);
    }

    //토큰 생성
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        try {
            return Jwts.builder()
                    .claims(claims) //기타 정보들 설정
                    .subject(subject)  //이메일
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(getSigningKey())//위조 방지를 위한 비밀키로 서명 생성
                    .compact();
        } catch (Exception e) {
            log.error("토큰 생성 실패: ", e);
            throw new TokenException(ErrorStatus.TOKEN_CREATION_FAILED);
        }
    }

    //토큰 유효성 검증
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    //토큰 유효성 검증 -만료되었는지 검증
    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new TokenException(ErrorStatus.TOKEN_INVALID_FORMAT);
    }

    public Authentication extractAuthentication(HttpServletRequest request) {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}