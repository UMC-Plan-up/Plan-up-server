package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InviteCodeProcessRequestDTO {

    @Schema(description = "처리할 초대코드", example = "123456")
    @NotBlank(message = "초대코드를 입력해주세요")
    private String inviteCode;
}
