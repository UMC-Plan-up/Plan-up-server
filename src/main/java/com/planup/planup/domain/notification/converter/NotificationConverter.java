package com.planup.planup.domain.notification.converter;

import com.planup.planup.domain.notification.dto.NotificationResponseDTO;
import com.planup.planup.domain.notification.entity.Notification;

import java.util.List;
import java.util.stream.Collectors;

public class NotificationConverter {

    public static List<NotificationResponseDTO.NotificationDTO> toNotificationDTOs(List<Notification> notifications) {
        return notifications.stream().map(NotificationConverter::toNotificationDTO).collect(Collectors.toList());
    }

    public static NotificationResponseDTO.NotificationDTO toNotificationDTO(Notification notification) {
        return NotificationResponseDTO.NotificationDTO.builder()
                .notificationText(notification.getNotificationMessage())
                .url(notification.getNotificationUrl())
                .id(notification.getId())
                .build();
    }
}
