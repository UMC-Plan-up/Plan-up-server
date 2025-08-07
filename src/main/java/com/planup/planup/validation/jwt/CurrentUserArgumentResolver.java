package com.planup.planup.validation.jwt;

import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.domain.user.entity.User;
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
    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(CurrentUser.class);
        boolean isLongType = parameter.getParameterType().equals(Long.class);
        boolean isUserType = parameter.getParameterType().equals(User.class);

        log.debug("supportsParameter - hasAnnotation: {}, isLongType: {}, isUserType: {}", hasAnnotation, isLongType, isUserType);

        return hasAnnotation && (isLongType || isUserType);
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
            throw new RuntimeException("인증 토큰이 없습니다");
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

            // Long 타입을 요구하는 경우 userId를 반환, User 타입을 요구하는 경우 user 객체를 반환
            if (parameter.getParameterType().equals(Long.class)) {
                return userId;
            } else {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
                return user;
            }

        } catch (Exception e) {
            log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("인증 토큰 처리에 실패했습니다");
        }
    }
}