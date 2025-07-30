package com.planup.planup.domain.goal.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.service.GoalService;
import com.planup.planup.validation.jwt.JwtUtil;
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

    @PatchMapping("/{goalId}/active")
    @Operation(summary = "목표 활성화/비활성화 API", description = "목표의 활성화 상태를 전환합니다.")
    public ApiResponse<Void> updateActiveGoal(
            @PathVariable Long goalId,
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

        goalService.updateActiveGoal(goalId, userId);

        return ApiResponse.onSuccess(null);
    }

    @PatchMapping("/{goalId}/public")
    @Operation(summary = "목표 공개/비공개 API", description = "목표의 공개 상태를 전환합니다.")
    public ApiResponse<Void> updatePublicGoal(
            @PathVariable Long goalId,
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

        goalService.updatePublicGoal(goalId, userId);

        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{goalId}/edit")
    @Operation(summary = "목표 수정 페이지 정보 조회 API", description = "수정을 위한 기존 목표 정보를 조회합니다.")
    public ApiResponse<GoalRequestDto.CreateGoalDto> getGoalForEdit(
            @PathVariable Long goalId,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        Long userId;

        // 테스트용 userId 추출
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            userId = 1L;
        } else {
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            String username = jwtUtil.extractUsername(token);
            userId = Long.parseLong(username);
        }

        GoalRequestDto.CreateGoalDto result = goalService.getGoalInfoToUpdate(goalId, userId);

        return ApiResponse.onSuccess(result);
    }

    @PutMapping("/{goalId}")
    @Operation(summary = "목표 수정 API", description = "기존 목표를 수정합니다.")
    public ApiResponse<Void> updateGoal(
            @PathVariable Long goalId,
            @Valid @RequestBody GoalRequestDto.CreateGoalDto dto,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        Long userId;

        // 테스트용 userId 추출
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            userId = 1L;
        } else {
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            String username = jwtUtil.extractUsername(token);
            userId = Long.parseLong(username);
        }

        goalService.updateGoal(goalId, userId, dto);

        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/{goalId}")
    @Operation(summary = "목표 삭제 API", description = "목표를 삭제합니다.")
    public ApiResponse<String> deleteGoal(
            @PathVariable Long goalId,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        Long userId;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            userId = 1L;
        } else {
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            String username = jwtUtil.extractUsername(token);
            userId = Long.parseLong(username);
        }

        goalService.deleteGoal(goalId, userId);

        return ApiResponse.onSuccess(null);
    }

    //랭킹 조회는 인증 서비스 만들고나서야 가능


}

