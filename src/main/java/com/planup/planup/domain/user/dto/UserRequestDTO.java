package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class UserRequestDTO {

    @Getter
    @Setter
    public static class Login {
        @NotBlank @Email @Schema(description = "이메일", example = "june5355@naver.com")
        private String email;

        @NotBlank @Schema(description = "비밀번호", example = "test1234!")
        private String password;
    }

    @Getter
    @Setter
    public static class PasswordChangeEmail {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        private String email;

        private Boolean isLoggedIn = false; // 로그인 상태
    }

    @Getter
    @Setter
    public static class Signup {
        @Schema(description = "이메일", example = "user@planup.com")
        @NotBlank
        @Email(message = "이메일 형식을 다시 확인해주세요 :)")
        private String email;

        @Schema(description = "비밀번호 (숫자+특수문자 포함)", example = "Planup123!")
        @Size(min = 8, max = 20)
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*]).+$",
                message = "비밀번호는 숫자와 특수문자를 포함해야 합니다.")
        private String password;

        @Schema(description = "비밀번호 확인", example = "Planup123!")
        @NotBlank
        private String passwordCheck;

        @Schema(description = "닉네임", example = "라미")
        @Size(max = 20, message = "닉네임은 공백 포함 20자 이하여야 합니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9 ]+$", message = "닉네임은 한글, 영문, 숫자, 공백만 가능합니다.")
        private String nickname;

        @Schema(description = "프로필 이미지 경로", example = "https://example.com/image.jpg")
        private String profileImg;

        @Schema(description = "약관 동의 목록")
        @NotEmpty
        private List<AuthRequestDTO.TermsAgreement> agreements;
    }

    @Getter
    @Setter
    public static class PasswordChangeWithToken {
        @NotBlank(message = "인증 토큰은 필수입니다.")
        private String token;

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        private String newPassword;
    }

    @Getter
    @Setter
    public static class Withdrawal {
        @Schema(description = "탈퇴 이유", example = "서비스 불만족")
        @NotBlank(message = "탈퇴 이유를 입력해주세요.")
        @Size(max = 100, message = "탈퇴 이유는 100자 이하로 입력해주세요.")
        private String reason;
    }

    @Getter
    @Setter
    public static class UpdateNickname {
        @NotBlank(message = "닉네임은 비어 있을 수 없습니다.")
        @Schema(example = "닉네임")
        @Size(max = 20, message = "닉네임은 공백 포함 20자 이하여야 합니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9 ]+$", message = "닉네임은 한글, 영문, 숫자, 공백만 가능합니다.")
        private String nickname;
    }
}
