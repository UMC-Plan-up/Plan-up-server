package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.goal.entity.PhotoChallenge;

public interface ChallengeService {
    Challenge createChallenge(Long userId, ChallengeRequestDTO.create request);
}
