package com.planup.planup.domain.user.dto;

import com.planup.planup.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserResponseDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Login {
        @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUz...")
        private String accessToken;

        @Schema(description = "JWT 리프레시 토큰", example = "eyJhbGciOiJIUz...")
        private String refreshToken;

        @Schema(description = "액세스 토큰 만료 시간 (초)", example = "3600")
        private Long expiresIn;

        @Schema(description = "회원 닉네임", example = "라미")
        private String nickname;

        @Schema(description = "프로필 이미지")
        private String profileImgUrl;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Signup {
        @Schema(description = "회원 고유 ID", example = "1")
        private Long id;

        @Schema(description = "이메일", example = "user@planup.com")
        private String email;

        @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String accessToken;

        @Schema(description = "JWT 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String refreshToken;

        @Schema(description = "액세스 토큰 만료 시간 (초)", example = "3600")
        private Long expiresIn;

        private RandomNickname userInfo;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RandomNickname {
        @Schema(description = "랜덤 닉네임", example = "행복한고양이")
        private String nickname;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Withdrawal {
        @Schema(description = "탈퇴 성공 여부", example = "true")
        private boolean success;

        @Schema(description = "탈퇴 완료 메시지", example = "회원 탈퇴가 완료되었습니다.")
        private String message;

        @Schema(description = "탈퇴 처리 시간")
        private String withdrawalDate;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String nickname;
        private String profileImg;
        private Boolean serviceNotificationAllow; // 서비스 알림 동의 상태
        private Boolean marketingNotificationAllow; // 혜택 및 마케팅 알림 동의 상태

        public static UserResponseDTO.UserInfo from(User user) {
            return UserResponseDTO.UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .profileImg(user.getProfileImg())
                    .serviceNotificationAllow(user.getServiceNotificationAllow())
                    .marketingNotificationAllow(user.getMarketingNotificationAllow())
                    .build();
        }
    }
}
