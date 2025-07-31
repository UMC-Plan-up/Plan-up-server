package com.planup.planup.domain.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InviteCodeResponseDTO {
    private String inviteCode;

    public static InviteCodeResponseDTO of(String code) {
        return InviteCodeResponseDTO.builder()
                .inviteCode(code)
                .build();
    }
}