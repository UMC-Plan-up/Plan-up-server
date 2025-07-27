package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TermsAgreementDTO {

    @Schema(description = "약관 ID", example = "1")
    @NotNull
    private Long termsId;

    @Schema(description = "동의 여부", example = "true")
    private boolean isAgreed;
}
