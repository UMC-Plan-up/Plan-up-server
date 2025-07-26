package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.goal.entity.Enum.GoalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.time.LocalDateTime;

public class ChallengeResponseDTO {

    @Builder
    public record ChallengeResponseInfo(
            @Schema(description = "id", example = "1")
            Long id,

            @Schema(description = "목표 이름", example = "매일 물 2L 마시기")
            String goalName,

            @Schema(description = "목표 달성량", example = "2000ml")
            String goalAmount,

            @Schema(description = "목표 유형", example = "TIME")
            GoalType goalType,

            @Schema(description = "목표 종료일", example = "2025-08-01T00:00:00.000Z")
            LocalDateTime endDate,

            @Schema(description = "주기(종료일까지 몇일 해야하는지)", example = "7")
            int timePerPeriod,

            @Schema(description = "기간 내 수행 빈도", example = "3")
            int frequency,

            @Schema(description = "목표 시간 (초 단위)", example = "7200")
            Long targetTime
    ) {}
}
