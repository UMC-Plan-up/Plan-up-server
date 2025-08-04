package com.planup.planup.validation.jwt;

import com.planup.planup.validation.annotation.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtUtil jwtUtil;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(CurrentUser.class);
        boolean isLongType = parameter.getParameterType().equals(Long.class);

        log.debug("supportsParameter - hasAnnotation: {}, isLongType: {}", hasAnnotation, isLongType);

        return hasAnnotation && isLongType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        log.debug("resolveArgument 호출됨 - parameter: {}", parameter.getParameterName());

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String authHeader = request.getHeader("Authorization");

        log.debug("Authorization Header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Authorization 헤더 없음, 기본값 1L 반환");
            return 1L;
        }

        try {
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            log.debug("추출된 토큰: {}", token != null ? "존재" : "null");

            // JWT 토큰 유효성 검증
            if (!jwtUtil.validateToken(token)) {
                log.error("유효하지 않은 토큰");
                throw new RuntimeException("유효하지 않은 토큰입니다");
            }

            Long userId = jwtUtil.extractUserId(token);
            log.debug("추출된 userId: {}", userId);

            //userId가 null이나 0 이하인지 확인
            if (userId == null || userId <= 0) {
                log.error("유효하지 않은 userId: {}", userId);
                throw new RuntimeException("유효하지 않은 사용자 ID입니다");
            }

            return userId;

        } catch (Exception e) {
            log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("인증 토큰 처리에 실패했습니다");
        }
    }
}