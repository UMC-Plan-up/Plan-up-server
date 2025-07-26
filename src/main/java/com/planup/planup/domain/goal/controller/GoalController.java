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

import java.util.List;

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

    @GetMapping("/my")
    @Operation(summary = "내 목표 조회 API", description = "사용자의 목표 목록을 조회하는 API입니다.")
    public ApiResponse<List<GoalResponseDto.MyGoalListDto>> getMyGoals(
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        Long userId;

        //테스트용, 로그인 구현 완료 후 토큰 발행 시 주석 처리된 코드로 되돌리기
        //중복 너무 많음 -> 추후 커스텀 어노테이션으로 수정
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

        List<GoalResponseDto.MyGoalListDto> result = goalService.getMyGoals(userId);

        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/{goalId}")
    @Operation(summary = "내 목표 상세 조회 API", description = "특정 목표의 상세 정보를 조회하는 API입니다.")
    public ApiResponse<GoalResponseDto.MyGoalDetailDto> getGoalDetail(
            @PathVariable Long goalId,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        Long userId;

        //테스트용, 로그인 구현 완료 후 토큰 발행 시 주석 처리된 코드로 되돌리기
        //중복 너무 많음 -> 추후 커스텀 어노테이션으로 수정
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

        GoalResponseDto.MyGoalDetailDto result = goalService.getMyGoalDetails(goalId, userId);

        return ApiResponse.onSuccess(result);
    }
}

