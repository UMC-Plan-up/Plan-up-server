package com.planup.planup.domain.notification.service;

import com.planup.planup.domain.notification.dto.NotificationResponseDTO;
import com.planup.planup.domain.notification.entity.Notification;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationServiceRead {
    //유저의 읽지 않은 알림을 시간 순 대로 가져온다
    @Transactional(readOnly = true)
    List<NotificationResponseDTO.NotificationDTO> getUnreadNotifications(Long receiverId);

    @Transactional(readOnly = true)
    List<NotificationResponseDTO.NotificationDTO> getUnreadNotificationsWithType(Long receiverId, String type);

    //유저의 모든 알림을 조회한다. (시간 순대로)
    @Transactional(readOnly = true)
    List<NotificationResponseDTO.NotificationDTO> getAllNotifications(Long receiverId);

    //유저에 따라 가장 최근의 5개 알림을 반환한다. (읽음 여부와 상관없이)
    @Transactional(readOnly = true)
    List<NotificationResponseDTO.NotificationDTO> getTop5RecentByUser(Long userId);

    Notification getById(Long id);
}
