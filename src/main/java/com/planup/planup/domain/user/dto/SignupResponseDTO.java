package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SignupResponseDTO {

    @Schema(description = "회원 고유 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "user@planup.com")
    private String email;

    @Schema(description = "초대코드로 친구가 된 사용자 닉네임", example = "라미")
    private String friendNickname;

    private String accessToken;

    private UserInfoResponseDTO userInfo;
}
