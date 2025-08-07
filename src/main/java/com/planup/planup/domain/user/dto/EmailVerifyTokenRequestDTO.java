package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerifyTokenRequestDTO {

    @Schema(description = "인증 토큰", example = "a9ad8668-ca3f-459b-ae9b-e14f4f174e39")
    @NotBlank(message = "토큰은 필수입니다")
    private String token;
}