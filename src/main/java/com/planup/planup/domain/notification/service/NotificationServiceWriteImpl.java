package com.planup.planup.domain.notification.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.NotificationError;
import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.notification.entity.NotificationType;
import com.planup.planup.domain.notification.entity.TargetType;
import com.planup.planup.domain.notification.entity.device.NotificationCreatedEvent;
import com.planup.planup.domain.notification.repository.NotificationRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.query.UserQueryService;
import jakarta.persistence.EntityNotFoundException;
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
    private final UserQueryService userService;
    private final ApplicationEventPublisher eventPublisher;

    //새로운 알림을 만든다.
    @Override
    public Notification createNotification(Long receiverId, Long senderId, NotificationType notificationType, TargetType targetType, Long targetId) {

        User receiver = userService.getUserByUserId(receiverId);
        User sender = userService.getUserByUserId(senderId);

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

        User receiver = userService.getUserByUserId(receiverId);
        User sender = userService.getUserByUserId(senderId);

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

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        validateReceiver(notification, userId);

        notification.markAsRead(true);
    }

    //해당 변경을 할 수 있는 권한이 있는지 확인
    private void validateReceiver(Notification notification, Long userId) {
        User user = userService.getUserByUserId(userId);
        if (!notification.getReceiver().equals(user)) {
            throw new NotificationError(ErrorStatus.UNAUTHORIZED_NOTIFICATION_ACCESS);
        }
    }
}
