package com.planup.planup.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record NotificationReadRequest(
        @Schema(description = "읽음 처리할 알림 ID 목록", example = "[1,2,3]")
        List<Long> notificationIdList
) {
}
