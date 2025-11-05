package com.planup.planup.domain.verification.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.friend.service.FriendService;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.service.UserGoalService;
import com.planup.planup.domain.user.service.UserService;
import com.planup.planup.domain.verification.convertor.TimerVerificationConverter;
import com.planup.planup.domain.verification.dto.TimerVerificationResponseDto;
import com.planup.planup.domain.verification.service.PhotoVerificationService;
import com.planup.planup.domain.verification.service.TimerVerificationReadService;
import com.planup.planup.domain.verification.service.TimerVerificationService;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/verification")
public class VerificationController {

    private final TimerVerificationReadService timerVerificationReadService;
    private final TimerVerificationService timerVerificationService;
    private final PhotoVerificationService photoVerificationService;
    private final UserGoalService userGoalService;
    private final FriendService friendService;
    private final UserService userService;

    @PostMapping("/timer/start")
    @Operation(summary = "타이머 시작 API", description = "선택한 목표의 타이머 인증을 시작합니다.")
    public ApiResponse<TimerVerificationResponseDto.TimerStartResponseDto> startTimer(
            @RequestParam(name = "goalId") Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        TimerVerificationResponseDto.TimerStartResponseDto result =
                timerVerificationService.startTimer(userId, goalId);

        return ApiResponse.onSuccess(result);
    }

    @PutMapping("/timer/stop/{timerId}")
    @Operation(summary = "타이머 종료 API", description = "진행 중인 타이머를 종료합니다.")
    public ApiResponse<TimerVerificationResponseDto.TimerStopResponseDto> stopTimer(
            @PathVariable Long timerId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        TimerVerificationResponseDto.TimerStopResponseDto result =
                timerVerificationService.stopTimer(timerId, userId);

        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/timer/today-total")
    @Operation(summary = "오늘 총 타이머 시간 조회 API", description = "특정 목표의 오늘 총 타이머 시간을 조회합니다.")
    public ApiResponse<TimerVerificationResponseDto.TodayTotalTimeResponseDto> getTodayTotalTime(
            @RequestParam("GoalId") Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {


        UserGoal userGoal = userGoalService.getByGoalIdAndUserId(userId, goalId);
        LocalTime totalTime = timerVerificationReadService.getTodayTotalTimeByUserGoal(userGoal);

        TimerVerificationResponseDto.TodayTotalTimeResponseDto result =
                TimerVerificationConverter.toTodayTotalTimeResponse(totalTime);

        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/friend/{friendId}/timer/today-total")
    @Operation(summary = "친구의 오늘 총 타이머 시간 조회 API", description = "친구의 특정 목표에 대한 오늘 총 타이머 시간을 조회합니다.")
    public ApiResponse<TimerVerificationResponseDto.TodayTotalTimeResponseDto> getFriendTodayTotalTime(
            @Parameter(description = "친구 ID", example = "2")
            @PathVariable Long friendId,
            @Parameter(description = "목표 ID", example = "1")
            @RequestParam("goalId") Long goalId,
            @Parameter(hidden = true) @CurrentUser Long userId) {
        userService.getUserbyUserId(userId);
        friendService.isFriend(userId, friendId);

        UserGoal userGoal = userGoalService.getByGoalIdAndUserId(friendId, goalId);
        LocalTime totalTime = timerVerificationReadService.getTodayTotalTimeByUserGoal(userGoal);

        TimerVerificationResponseDto.TodayTotalTimeResponseDto result =
                TimerVerificationConverter.toTodayTotalTimeResponse(totalTime);

        return ApiResponse.onSuccess(result);
    }

    @PostMapping(value = "/photo/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "사진 인증 업로드 API", description = "선택한 목표에 대한 사진 인증을 업로드합니다.")
    public ApiResponse<Void> uploadPhotoVerification(
            @RequestParam("goalId") Long goalId,
            @RequestPart("photoFile") MultipartFile photoFile,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        photoVerificationService.uploadPhotoVerification(userId, goalId, photoFile);

        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping("/photo/{verificationId}")
    @Operation(summary = "사진 인증 삭제 API", description = "특정 사진 인증을 삭제합니다.")
    public ApiResponse<Void> deletePhotoVerification(
            @PathVariable Long verificationId,
            @Parameter(hidden = true) @CurrentUser Long userId) {

        photoVerificationService.deletePhotoVerification(userId, verificationId);

        return ApiResponse.onSuccess(null);
    }
}
