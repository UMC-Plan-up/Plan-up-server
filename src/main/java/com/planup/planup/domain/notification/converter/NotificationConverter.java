package com.planup.planup.domain.notification.converter;

import com.planup.planup.domain.notification.dto.NotificationResponseDTO;
import com.planup.planup.domain.notification.entity.notification.Notification;

import java.util.List;
import java.util.stream.Collectors;

public class NotificationConverter {

    public static List<NotificationResponseDTO.NotificationDTO> toNotificationDTOs(List<Notification> notifications) {
        return notifications.stream().map(NotificationConverter::toNotificationDTO).collect(Collectors.toList());
    }

    public static NotificationResponseDTO.NotificationDTO toNotificationDTO(Notification notification) {
        return new NotificationResponseDTO.NotificationDTO(
                notification.getId(),
                notification.getNotificationMessage(),
                notification.getNotificationUrl(),
                notification.getCreatedAt(),

                notification.getTargetId(),
                notification.getTargetType(),

                notification.getType(),
                notification.getGroup(),

                notification.getSender() != null ? notification.getSender().getId() : null,
                notification.getSender() != null ? notification.getSender().getNickname() : null,
                notification.getSender() != null ? notification.getSender().getProfileImg() : null,

                notification.getUpdatedGoalInfo(),

                notification.isRead()
        );
    }
}
