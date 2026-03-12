package com.planup.planup.domain.notification.dto;

import java.util.List;

public record NotificationReadRequest(
        List<Long> notificationIdList
) {
}
