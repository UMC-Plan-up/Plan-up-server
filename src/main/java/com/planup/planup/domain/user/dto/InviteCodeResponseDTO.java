package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InviteCodeResponseDTO {
    @Schema(description = "내 초대코드", example = "123456")
    private String inviteCode;

    public static InviteCodeResponseDTO of(String code) {
        return InviteCodeResponseDTO.builder()
                .inviteCode(code)
                .build();
    }
}