package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateNicknameRequestDTO {
    @NotBlank(message = "닉네임은 비어 있을 수 없습니다.") @Schema(example = "닉네임")
    @Size(max = 20, message = "닉네임은 공백 포함 20자 이하여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9 ]+$", message = "닉네임은 한글, 영문, 숫자, 공백만 가능합니다.")
    private String nickname;
}