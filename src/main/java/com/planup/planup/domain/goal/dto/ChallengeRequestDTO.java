package com.planup.planup.domain.goal.dto;

import com.planup.planup.domain.goal.entity.Enum.ChallengeStatus;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Getter
    public record create(
            @NotBlank
            @Schema(description = "목표 이름", example = "매일 물 2L 마시기")
            String goalName,

            @NotBlank
            @Schema(description = "목표 달성량", example = "2000ml")
            String goalAmount,

            @NotNull
            @Schema(description = "목표 유형", example = "TIME")
            GoalType goalType,

            @NotBlank
            @Schema(description = "1회 기준량", example = "250ml")
            String oneDose,

            @NotNull
            @Schema(description = "목표 종료일", example = "2025-08-01T00:00:00.000Z")
            LocalDateTime endDate,

            @NotNull
            @Schema(description = "챌린지 상태", example = "PENDING")
            ChallengeStatus status,

            @NotBlank
            @Schema(description = "챌린지 실패 시 벌칙", example = "물 4L 마시기")
            String penalty,

            @Size(min = 1)
            @Schema(description = "같이 할 친구 선택", example = "1")
            List<Long> friendIdList,

            //선택 필듣
            @Valid createPhoto photoChallenge,
            @Valid createTime timeChallenge

    ) {}

    @Builder
    @Schema(description = "사진 챌린지 생성 요청")
    @Getter
    public record createPhoto(

            @Min(1)
            @Schema(description = "주기(며칠마다 1회)", example = "7")
            int timePerPeriod,

            @Min(1)
            @Schema(description = "기간 내 수행 빈도", example = "3")
            int frequency
            ) {}

    @Builder
    @Schema(description = "시간 챌린지 생성 요청")
    @Getter
    public record createTime(
            @NotNull
            @Positive
            @Schema(description = "목표 시간 (초 단위)", example = "7200")
            Long targetTime
    ) {}

}
