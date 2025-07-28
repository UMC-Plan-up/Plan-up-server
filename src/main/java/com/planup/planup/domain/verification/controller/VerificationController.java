package com.planup.planup.domain.verification.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.verification.convertor.TimerVerificationConverter;
import com.planup.planup.domain.verification.dto.TimerVerificationResponseDto;
import com.planup.planup.domain.verification.service.TimerVerificationService;
import com.planup.planup.validation.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;

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

    @PutMapping("/timer/stop/{timerId}")
    @Operation(summary = "타이머 종료 API", description = "진행 중인 타이머를 종료합니다.")
    public ApiResponse<TimerVerificationResponseDto.TimerStopResponseDto> stopTimer(
            @PathVariable Long timerId,
            HttpServletRequest request) {

        Long userId = extractUserId(request);

        TimerVerificationResponseDto.TimerStopResponseDto result =
                timerVerificationService.stopTimer(timerId, userId);

        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/timer/today-total")
    @Operation(summary = "오늘 총 타이머 시간 조회 API", description = "특정 목표의 오늘 총 타이머 시간을 조회합니다.")
    public ApiResponse<TimerVerificationResponseDto.TodayTotalTimeResponseDto> getTodayTotalTime(
            @RequestParam("userGoalId") Long userGoalId,
            HttpServletRequest request) {

        Long userId = extractUserId(request);

        LocalTime totalTime = timerVerificationService.getTodayTotalTime(userGoalId);

        TimerVerificationResponseDto.TodayTotalTimeResponseDto result =
                TimerVerificationConverter.toTodayTotalTimeResponse(totalTime);

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
