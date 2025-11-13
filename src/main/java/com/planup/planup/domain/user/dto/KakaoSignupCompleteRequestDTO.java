package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "카카오 회원가입 완료 요청")
public class KakaoSignupCompleteRequestDTO {
    @NotBlank(message = "임시 사용자 ID는 필수입니다")
    @Schema(description = "임시 사용자 ID", example = "1234")
    private String tempUserId;

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(max = 20, message = "닉네임은 공백 포함 20자 이하여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9 ]+$", message = "닉네임은 한글, 영문, 숫자, 공백만 가능합니다(특수문자 불가)")
    @Schema(description = "사용자 닉네임", example = "테스트유저")
    private String nickname;

    @Schema(description = "프로필 이미지 URL")
    private String profileImg;
    
    @Schema(description = "약관 동의 정보", example = "[{\"termsId\": 1, \"isAgreed\": true}, {\"termsId\": 2, \"isAgreed\": true}]")
    private List<TermsAgreementRequestDTO> agreements;
}