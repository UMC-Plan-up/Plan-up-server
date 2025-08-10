package com.planup.planup.domain.goal.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.service.GoalService;
import com.planup.planup.domain.verification.dto.PhotoVerificationResponseDto;
import com.planup.planup.validation.annotation.CurrentUser;
import com.planup.planup.validation.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/goals")
public class GoalController {
    private final GoalService goalService;

    @PostMapping("/create")
    @Operation(summary = "목표 생성 API", description = "목표를 생성하는 API입니다.")
    public ApiResponse<GoalResponseDto.GoalResultDto> createGoal(
            @Valid @RequestBody GoalRequestDto.CreateGoalDto dto,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        GoalResponseDto.GoalResultDto result = goalService.createGoal(userId, dto);

        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/create/list")
    @Operation(summary = "카테고리별 목표 조회 API", description = "목표 생성 시 선택한 카테고리의 친구/커뮤니티 목표 목록을 조회하는 API입니다.")
    public ApiResponse<List<GoalResponseDto.GoalCreateListDto>> getGoalList(
            @RequestParam GoalCategory goalCategory,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        List<GoalResponseDto.GoalCreateListDto> result = goalService.getGoalList(userId, goalCategory);

        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/mygoal/list")
    @Operation(summary = "내 목표 조회 리스트 API", description = "내 목표 리스트들을 조회하는 API입니다.")
    public ApiResponse<List<GoalResponseDto.MyGoalListDto>> getMyGoals(
            @Parameter(hidden = true) @CurrentUser Long userId) {

        List<GoalResponseDto.MyGoalListDto> result = goalService.getMyGoals(userId);

        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/mygoal/{goalId}")
    @Operation(summary = "내 목표 상세 조회 API", description = "특정 목표의 상세 정보를 조회하는 API입니다.")
    public ApiResponse<GoalResponseDto.MyGoalDetailDto> getGoalDetail(
            @Parameter(description = "목표 ID", example = "1")
            @RequestParam Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        GoalResponseDto.MyGoalDetailDto result = goalService.getMyGoalDetails(goalId, userId);

        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/friendgoal/list")
    @Operation(summary = "친구 목표 조회 리스트 API", description = "친구 목표 리스트들을 조회하는 API입니다.")
    public ApiResponse<List<GoalResponseDto.FriendGoalListDto>> getFriendGoals(
            @RequestParam Long friendId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        List<GoalResponseDto.FriendGoalListDto> result = goalService.getFriendGoals(userId,friendId);

        return ApiResponse.onSuccess(result);
    }

    @PatchMapping("/{goalId}/active")
    @Operation(summary = "목표 활성화/비활성화 API", description = "목표의 활성화 상태를 전환합니다.")
    public ApiResponse<Void> updateActiveGoal(
            @Parameter(description = "목표 ID", example = "1")
            @RequestParam Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        goalService.updateActiveGoal(goalId, userId);

        return ApiResponse.onSuccess(null);
    }

    @PatchMapping("/{goalId}/public")
    @Operation(summary = "목표 공개/비공개 API", description = "목표의 공개 상태를 전환합니다.")
    public ApiResponse<Void> updatePublicGoal(
            @Parameter(description = "목표 ID", example = "1")
            @RequestParam Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        goalService.updatePublicGoal(goalId, userId);

        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{goalId}/edit")
    @Operation(summary = "목표 수정 페이지 정보 조회 API", description = "수정을 위한 기존 목표 정보를 조회합니다.")
    public ApiResponse<GoalRequestDto.CreateGoalDto> getGoalForEdit(
            @Parameter(description = "목표 ID", example = "1")
            @RequestParam Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        GoalRequestDto.CreateGoalDto result = goalService.getGoalInfoToUpdate(goalId, userId);

        return ApiResponse.onSuccess(result);
    }

    @PutMapping("/{goalId}")
    @Operation(summary = "목표 수정 API", description = "기존 목표를 수정합니다.")
    public ApiResponse<Void> updateGoal(
            @Parameter(description = "목표 ID", example = "1")
            @RequestParam Long goalId,
            @Valid @RequestBody GoalRequestDto.CreateGoalDto dto,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        goalService.updateGoal(goalId, userId, dto);

        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/{goalId}")
    @Operation(summary = "목표 삭제 API", description = "목표를 삭제합니다.")
    public ApiResponse<String> deleteGoal(
            @Parameter(description = "목표 ID", example = "1")
            @RequestParam Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        goalService.deleteGoal(goalId, userId);

        return ApiResponse.onSuccess(null);
    }

    @PatchMapping("/{goalId}/ranking")
    @Operation(summary = "랭킹 API", description = "특정 목표의 랭킹을 제공합니다.")
    public ApiResponse<List<GoalResponseDto.RankingDto>> getGoalRanking(
            @RequestParam Long goalId) {
        List<GoalResponseDto.RankingDto> result = goalService.getGoalRanking(goalId);

        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/{goalId}/photos")
    @Operation(summary = "목표별 인증된 사진 조회 API", description = "특정 목표에 업로드한 인증된 사진들을 조회합니다.")
    public ApiResponse<List<PhotoVerificationResponseDto.uploadPhotoResponseDto>> getGoalPhotos(
            @Parameter(description = "목표 ID", example = "1")
            @RequestParam Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        List<PhotoVerificationResponseDto.uploadPhotoResponseDto> result = goalService.getGoalPhotos(userId, goalId);

        return ApiResponse.onSuccess(result);
    }


}

