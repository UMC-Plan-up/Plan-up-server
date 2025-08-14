package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.goal.entity.Enum.GoalPeriod;
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

            @Schema(description = "요청한 사람 이름", example = "홍길도")
            String name,

            @Schema(description = "목표 이름", example = "매일 물 2L 마시기")
            String goalName,

            @Schema(description = "목표 달성량", example = "2000ml")
            String goalAmount,

            @Schema(description = "목표 유형", example = "TIME")
            GoalType goalType,

            @Schema(description = "목표 종료일", example = "2025-08-01T00:00:00.000Z")
            LocalDateTime endDate,

            @Schema(description = "주기(종료일까지 몇일 해야하는지)", example = "7")
            GoalPeriod period,

            @Schema(description = "기간 내 수행 빈도", example = "3")
            int frequency,

            @Schema(description = "목표 시간 (초 단위)", example = "7200")
            Long targetTime
    ) {}

    @Builder
    public record ChallengeResultResponseDTO(
            @Schema(description = "id", example = "1")
            Long id,

            @Schema(description = "내 이름", example = "홍길도")
            String myName,

            @Schema(description = "내 프로필")
            String myProfile,

            @Schema(description = "챌린지 상대방 이름", example = "홍길도")
            String friendName,

            @Schema(description = "내 프로필")
            String friendProfile,

            @Schema(description = "패널티", example = "커피 사기")
            String penalty,

            @Schema(description = "내 달성률", example = "87")
            int myPercent,

            @Schema(description = "친구 달성률", example = "78")
            int friendPercent
    ) {}
}
