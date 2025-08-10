package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@NoArgsConstructor
@Schema(description = "카카오 회원가입 요청 DTO")
public class KakaoSignupRequestDTO {

    @Schema(description = "임시 토큰", example = "temp_token_123")
    @NotBlank(message = "임시 토큰은 필수입니다")
    private String tempToken;

    @Schema(description = "닉네임", example = "라미")
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다")
    private String nickname;
}