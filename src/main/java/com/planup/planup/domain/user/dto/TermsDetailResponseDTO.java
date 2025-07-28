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
@Schema(description = "약관 상세 응답 (팝업 표시용)")
public class TermsDetailResponseDTO {

    @Schema(description = "약관 ID", example = "1")
    private Long id;

    @Schema(description = "약관 상세 내용")
    private String content;
}
