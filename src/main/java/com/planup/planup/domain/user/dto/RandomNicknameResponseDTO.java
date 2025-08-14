package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RandomNicknameResponseDTO {

    @Schema(description = "랜덤 닉네임", example = "행복한고양이")
    private String nickname;

    @Schema(description = "형용사", example = "행복한")
    private String adjective;

    @Schema(description = "명사", example = "고양이")
    private String noun;
}
