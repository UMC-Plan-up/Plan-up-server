package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequestDTO {

    @Schema(description = "이메일", example = "user@planup.com")
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "이메일 형식을 확인해주세요")
    private String email;
}
