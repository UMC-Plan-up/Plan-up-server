package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "카카오 로그인 응답 DTO")
public class KakaoLoginResponseDTO {

    @Schema(description = "응답 상태", example = "LOGIN_SUCCESS")
    private String status;

    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUz...")
    private String accessToken;

    @Schema(description = "회원 닉네임", example = "라미")
    private String nickname;

    @Schema(description = "프로필 이미지")
    private String profileImgUrl;

    @Schema(description = "임시 토큰", example = "temp_token_123")
    private String tempToken;

    @Schema(description = "이메일", example = "user@kakao.com")
    private String email;
}
