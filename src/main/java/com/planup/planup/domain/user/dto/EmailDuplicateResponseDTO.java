package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDuplicateResponseDTO {

    @Schema(description = "이메일 사용 가능 여부", example = "true")
    private boolean available;

    @Schema(description = "응답 메시지", example = "사용 가능한 이메일입니다.")
    private String message;
}