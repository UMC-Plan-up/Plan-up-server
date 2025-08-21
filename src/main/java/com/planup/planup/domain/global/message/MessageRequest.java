package com.planup.planup.domain.global.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;


public record MessageRequest(
        @Schema(description = "작성자 이름", example = "은지")
        @NotBlank
        String name,

        @Schema(description = "응원 메시지를 보낼 상황이나 맥락", example = "시험을 준비 중이에요")
        @NotBlank
        String context,

        @Schema(description = "작성자 id", example = "16")
        Long userId,

        @Schema(description = "목표 id", example = "[16]")
        List<Long> goalIdList
) {}
