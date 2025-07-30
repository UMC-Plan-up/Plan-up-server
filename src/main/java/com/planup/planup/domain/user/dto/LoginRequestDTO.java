package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDTO {

    @Schema(description = "이메일", example = "user@planup.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "비밀번호", example = "Planup123!")
    @NotBlank
    private String password;
}
