package com.planup.planup.domain.user.dto;

public record UserDailySummaryDTO(
        Long userId,
        String name,
        String profileImg,
        long time
) {
}
