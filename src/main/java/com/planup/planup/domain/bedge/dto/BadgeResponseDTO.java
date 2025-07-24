package com.planup.planup.domain.bedge.dto;

import com.planup.planup.domain.bedge.entity.BadgeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public class BadgeResponseDTO {

    @Builder
    public record SimpleBadgeDTO(
            @Schema(description = "배지 ID", example = "1")
            Long id,

            @Schema(description = "배지 이름", example = "연속 공부왕")
            String badgeName,

            @Schema(description = "배지 유형", example = "ACHIEVEMENT")
            BadgeType badgeType
    ) {}
}
