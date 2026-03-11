package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.goal.entity.Enum.ChallengeStatus;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.GoalPeriod;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class ChallengeRequestDTO {

    @Builder
    @Schema(description = "챌린지 생성 요청의 공통 필드")
    public record create(
            @NotBlank
            @Schema(description = "목표 이름", example = "매일 물 2L 마시기")
            String goalName,

            @NotBlank
            @Schema(description = "목표 달성량", example = "2000ml")
            String goalAmount,

            @NotNull
            @Schema(description = "목표 유형", example = "CHALLENGE_TIME")
            GoalType goalType,

            @Positive
            @Schema(description = "1회 기준량", example = "250")
            Integer oneDose,

            @NotNull
            @Schema(description = "목표 종료일", example = "2025-08-01T00:00:00.000Z")
            LocalDateTime endDate,

            @NotNull
            @Schema(description = "챌린지 상태", example = "REQUESTED")
            ChallengeStatus status,

            @NotBlank
            @Schema(description = "챌린지 실패 시 벌칙", example = "물 4L 마시기")
            String penalty,

            @Schema(description = "같이 할 친구 선택", example = "1")
            Long friendId,

            @NotNull
            @Min(1) @Max(7)
            @Schema(description = "주기(며칠마다 1회)", example = "")
            Long referencePeriod,

            @Min(1)
            @NotNull
            @Schema(description = "기간 내 수행 빈도 또는 시간", example = "3")
            Integer frequency

    ) {}

    @Builder
    @Schema(description = "챌린지 패널티 제안")
    public record ReRequestPenalty(
            @Schema(description = "id", example = "1")
            Long id,

            @NotBlank
            @Schema(description = "챌린지 실패 시 벌칙", example = "물 4L 마시기")
            String penalty,

            @Size(min = 1)
            @Schema(description = "같이 할 친구 선택", example = "1")
            List<Long> friendIdList
    ) {}

}
