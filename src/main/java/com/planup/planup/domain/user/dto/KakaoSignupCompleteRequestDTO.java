package com.planup.planup.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KakaoSignupCompleteRequestDTO {
    @NotBlank(message = "임시 사용자 ID는 필수입니다")
    private String tempUserId;

    @NotBlank(message = "닉네임은 필수입니다")
    private String nickname;

    private String profileImg;
    private List<TermsAgreementRequestDTO> agreements;
    private String inviteCode; // 선택사항
}