package com.planup.planup.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

public class OAuthResponseDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "OAuthKakaoAccountResponse")
    public static class KakaoAccount {
        private boolean isLinked; // 카카오톡 계정 연동 여부
        private String kakaoEmail; // 연동된 카카오톡 이메일 (연동되지 않은 경우 null)
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "OAuthKakaoAuthResponse")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class KakaoAuth {
        private boolean isNewUser;
        private String tempUserId;   // 신규 사용자인 경우
        private String accessToken;  // 기존 사용자인 경우
        private String refreshToken; // 기존 사용자인 경우
        private Long expiresIn;      // 기존 사용자인 경우
        private UserResponseDTO.UserInfo userInfo; // 기존 사용자인 경우
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "OAuthKakaoLinkResponse")
    public static class KaKaoLink {
        private boolean success;
        private String message;
        private String kakaoEmail;
        private UserResponseDTO.UserInfo userInfo;
    }

    @Builder
    public record KakaoLinkStatus (
            boolean isKakaoLinked
    ) {};
}
