package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;

public interface ChallengeService {
    void createChallenge(Long userId, ChallengeRequestDTO.create request);
}
