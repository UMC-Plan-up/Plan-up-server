package com.planup.planup.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class AuthRequestDTO {

    @Getter
    @Setter
    public static class EmailVerification {
        @Schema(description = "이메일", example = "user@planup.com")
        @Email @NotBlank(message = "이메일은 필수입니다")
        private String email;
    }

    @Getter
    @Setter
    public static class InviteCode {
        @Schema(description = "초대코드", example = "123456")
        @NotBlank(message = "초대코드를 입력해주세요")
        private String inviteCode;
    }

    @Getter
    @Setter
    public static class TermsAgreement {
        @Schema(description = "약관 ID", example = "1")
        @NotNull
        private Long termsId;

        @Schema(description = "동의 여부", example = "true")
        @JsonProperty("isAgreed")
        private Boolean agreed;

        public boolean isAgreed() {
            return this.agreed != null && this.agreed;
        }
    }
}
