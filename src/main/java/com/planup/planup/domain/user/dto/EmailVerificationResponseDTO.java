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
@Schema(description = "이메일 인증 완료 응답 DTO")
public class EmailVerificationResponseDTO {

    @Schema(description = "인증 성공 여부", example = "true")
    private Boolean verified;

    @Schema(description = "인증된 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "응답 메시지", example = "이메일 인증이 완료되었습니다.")
    private String message;
}