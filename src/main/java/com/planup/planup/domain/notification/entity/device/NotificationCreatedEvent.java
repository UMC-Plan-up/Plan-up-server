package com.planup.planup.domain.notification.entity.device;

import com.planup.planup.domain.notification.entity.NotificationType;
import com.planup.planup.domain.notification.entity.TargetType;

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
