package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
    @Schema(description = "사용자 닉네임", example = "테스트유저")
    private String nickname;

    @Schema(description = "프로필 이미지 URL")
    private String profileImg;
    
    @Schema(description = "약관 동의 정보", example = "[{\"termsId\": 1, \"isAgreed\": true}, {\"termsId\": 2, \"isAgreed\": true}]")
    private List<TermsAgreementRequestDTO> agreements;
    
    @Schema(description = "초대 코드 (선택사항)", example = "123456")
    private String inviteCode; // 선택사항
}