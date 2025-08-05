package com.planup.planup.domain.notification.service;

import com.planup.planup.domain.notification.dto.NotificationResponseDTO;
import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.notification.entity.NotificationType;
import com.planup.planup.domain.notification.entity.TargetType;
import com.planup.planup.domain.user.entity.User;

import java.util.List;

public interface NotificationService {

    // 읽지 않은 알림 조회 (정렬 포함)
    List<NotificationResponseDTO.NotificationDTO> getUnreadNotifications(Long receiverId);

    // 전체 알림 조회 (정렬 포함)
    List<NotificationResponseDTO.NotificationDTO> getAllNotifications(Long receiverId);

    // 읽음 처리
    void markAsRead(Long notificationId, Long userId);

    List<NotificationResponseDTO.NotificationDTO> getTop5RecentByUser(Long userId);

    Notification createNotification(Long receiverId, Long senderId, NotificationType notificationType, TargetType targetType, Long targetId);
}
