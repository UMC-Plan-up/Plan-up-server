package com.planup.planup.domain.notification.service.notification;

import com.planup.planup.domain.notification.entity.notification.Notification;
import com.planup.planup.domain.notification.entity.notification.NotificationType;
import com.planup.planup.domain.notification.entity.notification.TargetType;

import java.util.List;

public interface NotificationCommandService {
    //새로운 알림을 만든다.
    Notification createNotification(Long receiverId, Long senderId, NotificationType notificationType, TargetType targetType, Long targetId);

    Notification createNotification(Long receiverId, Long senderId, NotificationType notificationType, TargetType targetType, Long targetId, List<String> updatedParts);

    void markAsRead(Long notificationId, Long userId);
}
