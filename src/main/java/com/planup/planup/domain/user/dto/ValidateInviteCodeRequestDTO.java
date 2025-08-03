package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ValidateInviteCodeRequestDTO {

    @Schema(description = "검증할 초대코드", example = "123456")
    @NotBlank(message = "초대코드를 입력해주세요")
    private String inviteCode;
}
