package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendResponseDTO {

    @Schema(description = "이메일", example = "user@planup.com")
    @Email
    String email;

    @Schema(description = "응답 메시지", example = "이메일이 발송되었습니다")
    String message;

    @Schema(description = "인증 토큰", example = "a9ad8668-ca3f-459b-ae9b-e14f4f174e39")
    String verificationToken;
}
