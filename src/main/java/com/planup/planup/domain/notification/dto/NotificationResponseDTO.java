package com.planup.planup.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

public class NotificationResponseDTO {

    @Builder
    public record NotificationDTO(
            @Schema(description = "알림 아이디", example = "1")
            Long id,

            @Schema(description = "알림 내용", example = "친구 1님의 댓글 '벌써 이만큼 함?'")
            String notificationText,

            @Schema(description = "이동 링크", example = "/user/{userId}")
            String url,

            @Schema(description = "알림 일자", example = "2025-01-52")
            LocalDateTime createdAt
    ) {}
}