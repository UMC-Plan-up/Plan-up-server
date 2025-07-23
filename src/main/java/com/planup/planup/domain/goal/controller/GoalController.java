package com.planup.planup.domain.goal.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.service.GoalService;
import com.planup.planup.validation.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/goals")
public class GoalController {
    private final GoalService goalService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    @Operation(summary = "목표 생성 API", description = "목표를 생성하는 API입니다.")
    public ApiResponse<GoalResponseDto.GoalResultDto> createGoal(
            @Valid @RequestBody GoalRequestDto.CreateGoalDto dto,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        Long userId;

        //테스트용, 로그인 구현 완료 후 토큰 발행 시 주석 처리된 코드로 되돌리기
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            userId = 1L;
        } else {
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            String username = jwtUtil.extractUsername(token);
            userId = Long.parseLong(username);
        }
//        String token = jwtUtil.extractTokenFromHeader(authHeader);
//        String username = jwtUtil.extractUsername(token);
//        Long userId = Long.parseLong(username);

        GoalResponseDto.GoalResultDto result = goalService.createGoal(userId, dto);

        return ApiResponse.onSuccess(result);
    }
}
