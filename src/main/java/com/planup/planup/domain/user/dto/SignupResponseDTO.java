package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SignupResponseDTO {

    @Schema(description = "회원 고유 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "user@planup.com")
    private String email;

    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    private UserInfoResponseDTO userInfo;
}
