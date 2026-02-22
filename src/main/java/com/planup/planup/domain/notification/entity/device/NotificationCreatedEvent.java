package com.planup.planup.domain.notification.entity.device;

import com.planup.planup.domain.notification.entity.notification.NotificationType;
import com.planup.planup.domain.notification.entity.notification.TargetType;

public record NotificationCreatedEvent(
        Long notificationId,
        Long receiverId,
        NotificationType notificationType,
        TargetType targetType,
        Long targetId,

        String senderName,
        String receiverName,
        String updatedPartsStr
) {
}
