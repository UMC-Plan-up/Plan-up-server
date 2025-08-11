package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeLinkResponseDTO {

    @Schema(description = "변경 성공 여부", example = "true")
    Boolean success;

    @Schema(description = "이메일", example = "user@planup.com")
    String email;

    @Schema(description = "응답 메시지", example = "비밀번호 변경이 확인되었습니다")
    String message;

    @Schema(description = "딥링크 URL", example = "planup://password/change?email=user@planup.com&verified=true&token=abc123")
    String deepLinkUrl;

    @Schema(description = "변경 토큰", example = "a9ad8668-ca3f-459b-ae9b-e14f4f174e39")
    String token;
}