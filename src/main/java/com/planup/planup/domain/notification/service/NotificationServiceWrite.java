package com.planup.planup.domain.notification.service;

import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.notification.entity.NotificationType;
import com.planup.planup.domain.notification.entity.TargetType;

import java.util.List;

public interface NotificationServiceWrite {
    //새로운 알림을 만든다.
    Notification createNotification(Long receiverId, Long senderId, NotificationType notificationType, TargetType targetType, Long targetId);

    Notification createNotification(Long receiverId, Long senderId, NotificationType notificationType, TargetType targetType, Long targetId, List<String> updatedParts);

    void markAsRead(Long notificationId, Long userId);
}
