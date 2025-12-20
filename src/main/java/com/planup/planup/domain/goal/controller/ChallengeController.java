package com.planup.planup.domain.goal.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.friend.service.FriendReadService;
import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
import com.planup.planup.domain.goal.dto.ChallengeResponseDTO;
import com.planup.planup.domain.goal.service.ChallengeService;
import com.planup.planup.validation.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;
    private final FriendReadService friendService;


    @PostMapping("/create")
    @Operation(summary = "챌린지 생성 요청", description = "goal을 상속한 challenge를 생성하고 userGoal도 생성한다.")
    public ApiResponse<Void> createChallenge(@CurrentUser Long userId, @RequestBody @Valid ChallengeRequestDTO.create createDTO) {
        challengeService.createChallenge(userId, createDTO);
        return ApiResponse.onSuccess(null);
    }


    @GetMapping("/friends")
    @Operation(summary = "챌린지에서 친구 조회", description = "친구에게 신청하기 위해 친구 정보 찾기")
    public ApiResponse<List<FriendResponseDTO.FriendInfoSummary>> getFriendList(@CurrentUser Long userId) {
        List<FriendResponseDTO.FriendInfoSummary> requestedFriends = friendService.getRequestedFriends(userId);
        return ApiResponse.onSuccess(requestedFriends);
    }


    @GetMapping("/{challengeId}")
    @Operation(summary = "챌린지 정보 조회", description = "친구가 신청한 챌린지의 자세한 정보 확인")
    public ApiResponse<ChallengeResponseDTO.ChallengeResponseInfo> getChallengeInfo(@CurrentUser Long userId, Long challengeId) {
        ChallengeResponseDTO.ChallengeResponseInfo challenge = challengeService.getChallengeInfo(challengeId);
        return ApiResponse.onSuccess(challenge);
    }

    @GetMapping("/{challengeId}/reject")
    @Operation(summary = "챌린지 거절")
    public ApiResponse<Void> rejectChallengeRequest(@CurrentUser Long userId, @PathVariable Long challengeId) {
        challengeService.rejectChallengeRequest(userId, challengeId);
        return ApiResponse.onSuccess(null);
    }


    @GetMapping("/{challengeId}/accept")
    @Operation(summary = "챌린지 수락")
    public ApiResponse<Void> acceptChallengeRequest(@CurrentUser Long userId, @PathVariable Long challengeId) {
        challengeService.acceptChallengeRequest(userId, challengeId);
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/repenalty")
    @Operation(summary = "챌린지에 대한 다른 패널티 제안")
    public ApiResponse<Void> reRequestPenalty(@CurrentUser Long userId, @RequestBody @Valid ChallengeRequestDTO.ReRequestPenalty dto) {
        challengeService.reRequestPenalty(userId, dto);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{challengeId}/name")
    @Operation(summary = " 챌린지 이름 조회")
    public ApiResponse<String> requestChallengeName(@CurrentUser Long userId, @PathVariable Long challengeId) {
        String challengeName = challengeService.getChallengeName(userId, challengeId);
        return ApiResponse.onSuccess(challengeName);
    }

    @GetMapping("/{challengeId}/result")
    @Operation(summary = "챌린지의 결과를 확인한다.")
    public ApiResponse<ChallengeResponseDTO.ChallengeResultResponseDTO> requestChallengeResult(@CurrentUser Long userId, @PathVariable Long challengeId) {
        ChallengeResponseDTO.ChallengeResultResponseDTO challengeResult = challengeService.getChallengeResult(userId, challengeId);
        return ApiResponse.onSuccess(challengeResult);
    }

    @GetMapping("/{challendeId}/penalty_remind")
    @Operation(summary = "챌린지의 패자에게 리마인드 알림을 전송한다")
    public ApiResponse<Void> remindPenalty(@CurrentUser Long userId, Long challengeId) {
        challengeService.remindPenalty(userId, challengeId);
        return ApiResponse.onSuccess(null);
    }
}
