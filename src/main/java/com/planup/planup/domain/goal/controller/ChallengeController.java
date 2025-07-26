package com.planup.planup.domain.goal.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.friend.service.FriendService;
import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
import com.planup.planup.domain.goal.dto.ChallengeResponseDTO;
import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.goal.service.ChallengeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/challenge")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;
    private final FriendService friendService;


    @PostMapping("/create")
    @Operation(summary = "챌린지 생성 요청", description = "goal을 상속한 challenge를 생성하고 userGoal도 생성한다.")
    public ApiResponse<Void> craeteChallenge (Long userId, @RequestBody @Valid ChallengeRequestDTO.create createDTO) {
        challengeService.createChallenge(userId, createDTO);
        return ApiResponse.onSuccess(null);
    }


    @GetMapping("/friends")
    @Operation(summary = "챌린지에서 친구 조회", description = "친구에게 신청하기 위해 친구 정보 찾기")
    public ApiResponse<List<FriendResponseDTO.FriendInfoInChallengeCreate>> getFriendList(Long userId) {
        List<FriendResponseDTO.FriendInfoInChallengeCreate> frinedListInChallenge = friendService.getFrinedListInChallenge(userId);
        return ApiResponse.onSuccess(frinedListInChallenge);
    }


    @GetMapping("/{challengeId}")
    @Operation(summary = "챌린지 정보 조회", description = "친구가 신청한 챌린지의 자세한 정보 확인")
    public ApiResponse<ChallengeResponseDTO.ChallengeResponseInfo> getChallengeInfo(Long challengeId) {
        ChallengeResponseDTO.ChallengeResponseInfo challenge = challengeService.getChallengeInfo(challengeId);
        return ApiResponse.onSuccess(challenge);
    }

    @GetMapping("/{challengeId}/reject")
    @Operation(summary = "챌린지 거절")
    public ApiResponse<Void> rejectChallengeRequest(Long userId, @PathVariable Long challengeId) {
        challengeService.rejectChallengeRequest(userId, challengeId);
        return ApiResponse.onSuccess(null);
    }


    @GetMapping("/{challengeId}/accept")
    @Operation(summary = "챌린지 수락")
    public ApiResponse<Void> acceptChallengeRequest(Long userId, @PathVariable Long challengeId) {
        challengeService.acceptChallengeRequest(userId, challengeId);
        return ApiResponse.onSuccess(null);
    }
}
