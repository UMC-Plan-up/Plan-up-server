package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SignupRequestDTO {

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
    @Size(max = 20)
    private String nickname;

    @Schema(description = "초대 코드", example = "XYZ123")
    private String inviteCode;

    @Schema(description = "프로필 이미지 경로", example = "https://example.com/image.jpg")
    private String profileImg;

    @Schema(description = "약관 동의 목록")
    @NotEmpty
    private List<TermsAgreementRequestDTO> agreements;
}

