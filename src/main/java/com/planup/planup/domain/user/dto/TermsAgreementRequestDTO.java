package com.planup.planup.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "약관 동의 요청")
public class TermsAgreementRequestDTO {

    @Schema(description = "약관 ID", example = "1")
    @NotNull
    private Long termsId;

    @Schema(description = "동의 여부", example = "true")
    @JsonProperty("isAgreed")
    private Boolean agreed;  // Boolean으로 변경하고 필드명도 변경

    // 또는 기존 필드를 유지하면서 명시적으로 getter 추가
    public boolean isAgreed() {
        return this.agreed != null && this.agreed;
    }
}
