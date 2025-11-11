package com.planup.planup.domain.user.dto;

import com.planup.planup.domain.user.enums.TokenStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthResponseDTO {
    // 이메일, 초대코드, 약관
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailDuplicate {
        @Schema(description = "이메일 사용 가능 여부", example = "true")
        private boolean available;

        @Schema(description = "응답 메시지", example = "사용 가능한 이메일입니다.")
        private String message;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailSend {
        @Schema(description = "이메일", example = "user@planup.com")
        @Email
        String email;

        @Schema(description = "응답 메시지", example = "이메일이 발송되었습니다")
        String message;

        @Schema(description = "인증 토큰", example = "a9ad8668-ca3f-459b-ae9b-e14f4f174e39")
        String verificationToken;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailVerificationStatus {
        @Schema(description = "인증 완료 여부", example = "true")
        private Boolean verified;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "토큰 상태", example = "VALID")
        private TokenStatus tokenStatus;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailVerifyLink {
        private boolean verified;
        private String email;
        private String message;
        private String deepLinkUrl;
        private String token;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InviteCode {
        @Schema(description = "내 초대코드", example = "123456")
        private String inviteCode;

        public static AuthResponseDTO.InviteCode of(String code) {
            return AuthResponseDTO.InviteCode.builder()
                    .inviteCode(code)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InviteCodeProcess {
        @Schema(description = "초대코드로 친구가 된 사용자 닉네임", example = "라미")
        private String friendNickname;

        @Schema(description = "처리 결과 메시지", example = "친구 관계가 성공적으로 생성되었습니다.")
        private String message;

        @Schema(description = "처리 성공 여부", example = "true")
        private boolean success;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TermsDetail {
        @Schema(description = "약관 ID", example = "1")
        private Long id;

        @Schema(description = "약관 상세 내용")
        private String content;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TermsList {
        @Schema(description = "약관 ID", example = "1")
        private Long id;

        @Schema(description = "약관 요약", example = "서비스 이용약관")
        private String summary;

        @Schema(description = "필수 약관 여부", example = "true")
        private Boolean isRequired;

        @Schema(description = "약관 순서", example = "1")
        private Integer order;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidateInviteCode {
        @Schema(description = "초대코드 유효성", example = "true")
        private boolean valid;

        @Schema(description = "검증 결과 메시지", example = "유효한 초대코드입니다.")
        private String message;

        @Schema(description = "초대한 사용자 닉네임 (유효한 경우만)", example = "라미")
        private String targetUserNickname;
    }

}
