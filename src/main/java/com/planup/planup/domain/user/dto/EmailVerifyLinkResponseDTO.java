package com.planup.planup.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailVerifyLinkResponseDTO {

    private boolean verified;
    private String email;
    private String message;
    private String deepLinkUrl;
    private String token;
}
