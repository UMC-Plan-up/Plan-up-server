package com.planup.planup.domain.notification.service;

import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.user.entity.User;

import java.util.List;

public interface NotificationService {

    // 읽지 않은 알림 조회 (정렬 포함)
    List<Notification> getUnreadNotifications(Long receiverId);

    // 전체 알림 조회 (정렬 포함)
    List<Notification> getAllNotifications(Long receiverId);

    // 읽음 처리
    void markAsRead(Long notificationId);

    List<Notification> getTop5RecentByUser(Long userId);
}
