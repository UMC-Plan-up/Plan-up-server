package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "약관 정보 응답 (체크박스 표시용)")
public class TermsListResponseDTO {

    @Schema(description = "약관 ID", example = "1")
    private Long id;

    @Schema(description = "약관 요약", example = "서비스 이용약관")
    private String summary;

    // content 필드 제외 → 응답 크기 최적화

    @Schema(description = "필수 약관 여부", example = "true")
    private Boolean isRequired;

    @Schema(description = "약관 순서", example = "1")
    private Integer order;

}
