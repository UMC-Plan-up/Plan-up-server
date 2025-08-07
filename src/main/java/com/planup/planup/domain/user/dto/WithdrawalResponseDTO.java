package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public class WithdrawalResponseDTO {

    @Schema(description = "탈퇴 성공 여부", example = "true")
    private boolean success;

    @Schema(description = "탈퇴 완료 메시지", example = "회원 탈퇴가 완료되었습니다.")
    private String message;

    @Schema(description = "탈퇴 처리 시간")
    private String withdrawalDate;
}