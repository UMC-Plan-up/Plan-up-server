package com.planup.planup.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoLinkRequestDTO {
    @NotBlank(message = "카카오 인가코드는 필수입니다")
    private String code;
}
