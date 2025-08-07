package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public class WithdrawalRequestDTO {

    @Schema(description = "탈퇴 이유", example = "서비스 불만족")
    @NotBlank(message = "탈퇴 이유를 입력해주세요.")
    @Size(max = 100, message = "탈퇴 이유는 100자 이하로 입력해주세요.")
    private String reason;

    @Schema(description = "상세 탈퇴 이유 (선택사항)", example = "앱이 너무 느리고 버그가 많아서...")
    @Size(max = 1000, message = "상세 탈퇴 이유는 1000자 이하로 입력해주세요.")
    private String detailReason;

    public String getReason() {
        return reason;
    }

    public String getDetailReason() {
        return detailReason;
    }
}