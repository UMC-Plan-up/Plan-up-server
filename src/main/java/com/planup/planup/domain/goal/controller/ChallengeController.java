package com.planup.planup.domain.goal.controller;

import com.planup.planup.apiPayload.ApiResponse;
import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
import com.planup.planup.domain.goal.service.ChallengeService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/challenge")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping("/create")
    public ApiResponse<null> craeteChallenge (Long userId, @RequestBody ChallengeRequestDTO.create requestDTO) {
        challengeService.create()
    }
}
