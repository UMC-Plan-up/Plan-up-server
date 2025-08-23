package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
import com.planup.planup.domain.goal.dto.ChallengeResponseDTO;
import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChallengeService {
    @Transactional
    User getOtherMember(User user, Challenge challenge);

    Challenge createChallenge(Long user, ChallengeRequestDTO.create request);

    ChallengeResponseDTO.ChallengeResponseInfo getChallengeInfo(Long challengeId);

    void rejectChallengeRequest(Long userId, Long challengeId);

    void acceptChallengeRequest(Long userId, Long challengeId);

    void reRequestPenalty(Long userId, ChallengeRequestDTO.ReRequestPenalty dto);

    String getChallengeName(Long userId, Long challengeId);

    ChallengeResponseDTO.ChallengeResultResponseDTO getChallengeResult(Long userId, Long challengeId);

    void checkChallengeFin(UserGoal userGoal);

    @Transactional(readOnly = true)
    void remindPenalty(Long userId, Long challengeId);
}
