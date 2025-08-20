package com.planup.planup.domain.global.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record MessageRequest(
        @Schema(description = "작성자 이름", example = "은지")
        @NotBlank
        String name,

        @Schema(description = "응원 메시지를 보낼 상황이나 맥락", example = "시험을 준비 중이에요")
        @NotBlank
        String context,

        @Schema(description = "메시지의 톤 (밝게/차분하게/장난스럽게)", example = "밝게")
        @NotBlank
        String tone,

        @Schema(description = "말투의 형식 (존댓말/반말)", example = "존댓말")
        @NotBlank
        String formality,

        @Schema(description = "이모지를 포함할지 여부 (null이면 기본 true)", example = "true")
        Boolean emoji
) {}
