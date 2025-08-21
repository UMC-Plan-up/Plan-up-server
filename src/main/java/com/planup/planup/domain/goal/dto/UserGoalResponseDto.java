package com.planup.planup.domain.goal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

public class UserGoalResponseDto {

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalTotalAchievementDto {
        @Schema(description = "목표 ID", example = "1")
        private Long goalId;

        @Schema(description = "전체 달성률 (퍼센트)", example = "85")
        private int totalAchievementRate;
    }
}
