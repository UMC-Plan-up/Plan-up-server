package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@Schema(description = "카카오 로그인 요청 DTO")
public class KakaoLoginRequestDTO {

    @Schema(description = "카카오 인가코드", example = "abcdef123456")
    @NotBlank(message = "인가코드는 필수입니다")
    private String code;
}