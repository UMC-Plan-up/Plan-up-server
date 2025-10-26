package com.planup.planup.domain.user.dto;

import com.planup.planup.domain.user.entity.TokenStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationStatusResponseDTO {

    @Schema(description = "인증 완료 여부", example = "true")
    private Boolean verified;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "토큰 상태", example = "VALID")
    private TokenStatus tokenStatus;
}


