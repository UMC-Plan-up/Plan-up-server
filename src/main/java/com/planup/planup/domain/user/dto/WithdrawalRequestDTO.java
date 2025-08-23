package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequestDTO {

    @Schema(description = "탈퇴 이유", example = "서비스 불만족")
    @NotBlank(message = "탈퇴 이유를 입력해주세요.")
    @Size(max = 100, message = "탈퇴 이유는 100자 이하로 입력해주세요.")
    private String reason;

    public String getReason() {
        return reason;
    }
}