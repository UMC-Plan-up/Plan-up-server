package com.planup.planup.domain.notification.service;

import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.notification.entity.NotificationType;
import com.planup.planup.domain.notification.entity.TargetType;
import com.planup.planup.domain.notification.entity.device.NotificationCreatedEvent;
import com.planup.planup.domain.notification.repository.NotificationRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceWriteImpl implements NotificationServiceWrite {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    //새로운 알림을 만든다.
    @Override
    public Notification createNotification(Long receiverId, Long senderId, NotificationType notificationType, TargetType targetType, Long targetId) {

        User receiver = userService.getUserbyUserId(receiverId);
        User sender = userService.getUserbyUserId(senderId);

        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(notificationType)
                .targetType(targetType)
                .targetId(targetId)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        eventPublisher.publishEvent(new NotificationCreatedEvent(
                savedNotification.getId(), receiverId, notificationType, targetType, targetId, sender.getNickname(), receiver.getNickname(), null
                ));

        return savedNotification;
    }

    @Override
    public Notification createNotification(Long receiverId, Long senderId, NotificationType notificationType, TargetType targetType, Long targetId, List<String> updatedParts) {

        String updatedPartsStr = String.join(", ", updatedParts);

        User receiver = userService.getUserbyUserId(receiverId);
        User sender = userService.getUserbyUserId(senderId);

        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(notificationType)
                .targetType(targetType)
                .targetId(targetId)
                .updatedGoalInfo(updatedPartsStr)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        eventPublisher.publishEvent(new NotificationCreatedEvent(
                        savedNotification.getId(), receiverId, notificationType, targetType, targetId, sender.getNickname(), receiver.getNickname(), savedNotification.getUpdatedGoalInfo()
                ));

        return savedNotification;
    }
}
