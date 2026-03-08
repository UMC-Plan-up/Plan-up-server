package com.planup.planup.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.enums.Gender;
import com.planup.planup.domain.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class UserResponseDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "UserAuthResponse")
    public static class AuthResponseDTO {
        @Schema(description = "유저 상태", example = "LOGIN_SUCCESS")
        private UserStatus userStatus;

        @Schema(description = "임시 유저 ID (회원가입 필요 시)", example = "a1b2c3d4-e5f6-...")
        private String tempUserId;

        @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUz...")
        private String accessToken;

        @Schema(description = "JWT 리프레시 토큰", example = "eyJhbGciOiJIUz...")
        private String refreshToken;

        @Schema(description = "액세스 토큰 만료 시간 (초)", example = "3600")
        private Long expiresIn;

        @Schema(description = "유저 정보")
        private UserInfo userInfo;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "UserResponseUserInfo")
    public static class UserInfo {
        @Schema(description = "회원 고유 ID", example = "1")
        private Long id;

        @Schema(description = "이메일", example = "user@planup.com")
        private String email;

        @Schema(description = "이름", example = "김라미")
        private String name;

        @Schema(description = "닉네임", example = "라미")
        private String nickname;

        @Schema(description = "생년월일", example = "2000-01-01")
        private LocalDate birthDate;

        @Schema(description = "성별", example = "FEMALE")
        private Gender gender;

        @Schema(description = "프로필 이미지")
        private String profileImg;

        @Schema(description = "서비스 알림 동의 여부", example = "true")
        private Boolean serviceNotificationAllow;

        @Schema(description = "마케팅 알림 동의 여부", example = "false")
        private Boolean marketingNotificationAllow;

        public static UserInfo from(User user, boolean isServiceNotifi, boolean isMarketingNotifi) {
            return UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .nickname(user.getNickname())
                    .birthDate(user.getBirthDate())
                    .gender(user.getGender())
                    .profileImg(user.getProfileImg())
                    .serviceNotificationAllow(isServiceNotifi)
                    .marketingNotificationAllow(isMarketingNotifi)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "UserRandomNicknameResponse")
    public static class RandomNickname {
        @Schema(description = "랜덤 닉네임", example = "행복한고양이")
        private String nickname;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "UserWithdrawalResponse")
    public static class Withdrawal {
        @Schema(description = "탈퇴 성공 여부", example = "true")
        private boolean success;

        @Schema(description = "탈퇴 완료 메시지", example = "회원 탈퇴가 완료되었습니다.")
        private String message;

        @Schema(description = "탈퇴 처리 시간")
        private String withdrawalDate;
    }
}
