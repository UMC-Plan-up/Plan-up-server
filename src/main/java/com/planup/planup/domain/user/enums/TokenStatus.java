package com.planup.planup.domain.user.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 상태")
public enum TokenStatus {
    @Schema(description = "유효한 토큰")
    VALID,
    
    @Schema(description = "만료되었거나 유효하지 않은 토큰")
    EXPIRED_OR_INVALID
}
