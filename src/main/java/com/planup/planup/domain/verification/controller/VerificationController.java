package com.planup.planup.domain.verification.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.verification.dto.TimerVerificationResponseDto;
import com.planup.planup.domain.verification.service.TimerVerificationService;
import com.planup.planup.validation.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/verification")
public class VerificationController {

    private final TimerVerificationService timerVerificationService;
    private final JwtUtil jwtUtil;

    @PostMapping("/timer/start")
    @Operation(summary = "타이머 시작 API", description = "선택한 목표의 타이머 인증을 시작합니다.")
    public ApiResponse<TimerVerificationResponseDto.TimerStartResponseDto> startTimer(
            @RequestParam(name = "goalId") Long goalId,
            HttpServletRequest request) {

        Long userId = extractUserId(request);

        TimerVerificationResponseDto.TimerStartResponseDto result =
                timerVerificationService.startTimer(userId, goalId);

        return ApiResponse.onSuccess(result);
    }

    //다음 커밋때 리팩토링
    private Long extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return 1L;
        } else {
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            String username = jwtUtil.extractUsername(token);
            return Long.parseLong(username);
        }
    }

}
