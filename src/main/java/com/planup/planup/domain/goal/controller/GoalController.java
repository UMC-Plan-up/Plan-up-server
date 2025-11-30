package com.planup.planup.domain.goal.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.friend.service.FriendReadService;
import com.planup.planup.domain.goal.dto.CommentRequestDto;
import com.planup.planup.domain.goal.dto.CommentResponseDto;
import com.planup.planup.domain.goal.dto.GoalRequestDto;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.service.CommentService;
import com.planup.planup.domain.goal.service.GoalService;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import com.planup.planup.domain.verification.dto.PhotoVerificationResponseDto;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/goals")
public class GoalController {
    private final GoalService goalService;
    private final CommentService commentService;
    private final UserService userService;
    private final FriendReadService friendService;

    @PostMapping("/create")
    @Operation(summary = "목표 생성 API", description = "목표를 생성하는 API입니다.")
    public ApiResponse<GoalResponseDto.GoalResultDto> createGoal(
            @Valid @RequestBody GoalRequestDto.CreateGoalDto dto,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        GoalResponseDto.GoalResultDto result = goalService.createGoal(userId, dto);

        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/create/list/friend")
    @Operation(summary = "카테고리별 친구 목표 조회 API", description = "선택한 카테고리의 친구 목표 목록을 조회합니다.")
    public ApiResponse<List<GoalResponseDto.GoalCreateListDto>> getFriendGoalsByCategory(
            @RequestParam GoalCategory goalCategory,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        List<GoalResponseDto.GoalCreateListDto> result = goalService.getFriendGoalsByCategory(userId, goalCategory);
        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/create/list/community")
    @Operation(summary = "카테고리별 커뮤니티 목표 조회 API", description = "선택한 카테고리의 커뮤니티 목표 목록을 조회합니다.")
    public ApiResponse<List<GoalResponseDto.GoalCreateListDto>> getCommunityGoalsByCategory(
            @RequestParam GoalCategory goalCategory) {

        List<GoalResponseDto.GoalCreateListDto> result = goalService.getCommunityGoalsByCategory(goalCategory);
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

    //친구 목표 업로드 사진 조회
    @GetMapping("/friend/{friendId}/goal/{goalId}/photos")
    @Operation(summary = "친구 목표 인증 사진 조회 API", description = "친구의 특정 목표에 업로드한 인증 사진들을 조회합니다.")
    public ApiResponse<List<PhotoVerificationResponseDto.uploadPhotoResponseDto>> getFriendGoalPhotos(
            @PathVariable Long friendId,
            @PathVariable Long goalId,
            @CurrentUser Long userId) {
        User user = userService.getUserbyUserId(userId);
        friendService.isFriend(userId, friendId);

        List<PhotoVerificationResponseDto.uploadPhotoResponseDto> result =
                goalService.getGoalPhotos(friendId, goalId);

        return ApiResponse.onSuccess(result);
    }

    // 댓글 CRUD
    @PostMapping("/{goalId}/comments")
    @Operation(summary = "댓글 작성 API", description = "특정 목표에 댓글을 작성합니다.")
    public ApiResponse<CommentResponseDto.CommentDto> createComment(
            @PathVariable Long goalId,
            @Valid @RequestBody CommentRequestDto.CommentCreateRequestDto requestDto,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        CommentResponseDto.CommentDto result = commentService.createCommentByGoal(goalId, userId, requestDto);
        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/{goalId}/comments")
    @Operation(summary = "댓글 조회 API", description = "특정 목표의 댓글 목록을 조회합니다.")
    public ApiResponse<List<CommentResponseDto.CommentDto>> getComments(
            @PathVariable Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        List<CommentResponseDto.CommentDto> result = commentService.getComments(goalId, userId);
        return ApiResponse.onSuccess(result);
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "댓글 수정 API", description = "본인이 작성한 댓글을 수정합니다.")
    public ApiResponse<CommentResponseDto.CommentDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody String content,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        CommentResponseDto.CommentDto result = commentService.updateComment(commentId, userId, content);
        return ApiResponse.onSuccess(result);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제 API", description = "본인이 작성한 댓글을 삭제합니다.")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long commentId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        commentService.deleteComment(commentId, userId);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{goalId}/friendstimer")
    @Operation(summary = "친구 타이머 현황 조회 API", description = "특정 목표에 참여 중인 친구들의 타이머 현황을 조회합니다.")
    public ApiResponse<List<GoalResponseDto.FriendTimerStatusDto>> getFriendTimerStatus(
            @Parameter(description = "목표 ID", example = "1")
            @PathVariable Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        List<GoalResponseDto.FriendTimerStatusDto> result = goalService.getFriendTimerStatus(goalId, userId);

        return ApiResponse.onSuccess(result);
    }

    @PostMapping("/{goalId}/memo")
    @Operation(
            summary = "목표 메모 저장",
            description = "목표의 특정 날짜 메모를 생성/수정/삭제합니다. " +
                    "메모 내용이 있으면 생성 또는 수정, 빈 값이면 삭제됩니다."
    )
    public ApiResponse<GoalResponseDto.GoalMemoResponseDto> saveMemo(
            @Parameter(description = "목표 ID", example = "1")
            @PathVariable Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId,
            @Valid @RequestBody GoalRequestDto.CreateMemoRequestDto request) {

        GoalResponseDto.GoalMemoResponseDto response = goalService.saveMemo(
                userId, goalId, request);

        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/daily/{date}")
    @Operation(summary = "날짜별 인증한 목표 조회 API", description = "특정 날짜에 인증한 목표들의 리스트를 조회합니다.")
    public ApiResponse<GoalResponseDto.DailyVerifiedGoalsResponse> getDailyVerifiedGoals(
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", example = "2025-01-15")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        GoalResponseDto.DailyVerifiedGoalsResponse result = goalService.getDailyVerifiedGoals(userId, date);
        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/{goalId}/memo/{date}")
    @Operation(summary = "특정 날짜 메모 조회", description = "목표의 특정 날짜 메모를 조회합니다.")
    public ApiResponse<GoalResponseDto.GoalMemoReadDto> getMemo(
            @PathVariable Long goalId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        GoalResponseDto.GoalMemoReadDto memo = goalService.getMemo(userId, goalId, date);
        return ApiResponse.onSuccess(memo);
    }

    @GetMapping("/{goalId}/memo/period")
    @Operation(
            summary = "기간별 메모 조회",
            description = "목표의 특정 기간 동안의 메모를 조회합니다."
    )
    public ApiResponse<List<GoalResponseDto.GoalMemoReadDto>> getMemosByPeriod(
            @PathVariable Long goalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        List<GoalResponseDto.GoalMemoReadDto> memos = goalService.getMemosByPeriod(
                userId, goalId, startDate, endDate);

        return ApiResponse.onSuccess(memos);
    }

    @GetMapping("/{goalId}/reactions")
    @Operation(summary = "목표 반응 조회 API", description = "특정 목표의 응원/분발 카운트와 현재 사용자의 반응 여부를 조회합니다.")
    public ApiResponse<GoalResponseDto.GoalReactionDto> getGoalReactions(
            @Parameter(description = "목표 ID", example = "1")
            @PathVariable Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        GoalResponseDto.GoalReactionDto result = goalService.getGoalReactions(goalId, userId);
        return ApiResponse.onSuccess(result);
    }

    @PostMapping("/{goalId}/reactions/cheer")
    @Operation(summary = "응원하기 API", description = "특정 목표에 응원을 등록합니다. 하루에 한 번만 가능합니다.")
    public ApiResponse<GoalResponseDto.ReactionResultDto> addCheer(
            @Parameter(description = "목표 ID", example = "1")
            @PathVariable Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        GoalResponseDto.ReactionResultDto result = goalService.addCheer(goalId, userId);
        return ApiResponse.onSuccess(result);
    }

    @PostMapping("/{goalId}/reactions/encourage")
    @Operation(summary = "분발하기 API", description = "특정 목표에 분발을 등록합니다. 하루에 한 번만 가능합니다.")
    public ApiResponse<GoalResponseDto.ReactionResultDto> addEncourage(
            @Parameter(description = "목표 ID", example = "1")
            @PathVariable Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        GoalResponseDto.ReactionResultDto result = goalService.addEncourage(goalId, userId);
        return ApiResponse.onSuccess(result);
    }
}

