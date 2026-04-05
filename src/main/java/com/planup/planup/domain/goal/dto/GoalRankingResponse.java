package com.planup.planup.domain.goal.dto;

public record GoalRankingResponse(
        int rank,
        Long userId,
        String nickname,
        String profileImage,
        double score
) {
}
