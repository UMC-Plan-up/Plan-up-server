package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ValidateInviteCodeResponseDTO {

    @Schema(description = "초대코드 유효성", example = "true")
    private boolean valid;

    @Schema(description = "검증 결과 메시지", example = "유효한 초대코드입니다.")
    private String message;

    @Schema(description = "초대한 사용자 닉네임 (유효한 경우만)", example = "라미")
    private String targetUserNickname;
}
