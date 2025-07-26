package com.planup.planup.domain.goal.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.friend.service.FriendService;
import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
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
    public ApiResponse<List<FriendResponseDTO.FriendInfoInChallengeCreate>> getFriendList(Long userId) {
        List<FriendResponseDTO.FriendInfoInChallengeCreate> frinedListInChallenge = friendService.getFrinedListInChallenge(userId);
        return ApiResponse.onSuccess(frinedListInChallenge);
    }

    @GetMapping("/{challengeId}")
    public ApiResponse<Ch>
}
