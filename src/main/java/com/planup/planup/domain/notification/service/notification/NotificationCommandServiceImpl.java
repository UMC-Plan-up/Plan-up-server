package com.planup.planup.domain.notification.service.notification;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.NotificationError;
import com.planup.planup.domain.notification.entity.device.NotificationTokenPreference;
import com.planup.planup.domain.notification.entity.notification.Notification;
import com.planup.planup.domain.notification.entity.notification.NotificationGroup;
import com.planup.planup.domain.notification.entity.notification.NotificationType;
import com.planup.planup.domain.notification.entity.notification.TargetType;
import com.planup.planup.domain.notification.entity.device.NotificationCreatedEvent;
import com.planup.planup.domain.notification.repository.NotificationRepository;
import com.planup.planup.domain.notification.service.NotificationPreferenceService;
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
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final UserQueryService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationPreferenceService preference;

    //새로운 알림을 만든다.
    @Override
    public Notification createNotification(Long receiverId, Long senderId, NotificationType notificationType, TargetType targetType, Long targetId, NotificationGroup group) {

        User receiver = userService.getUserByUserId(receiverId);
        User sender = userService.getUserByUserId(senderId);

        //수신 설정 되어있는지 확인
        boolean enabled = preference.isEnabled(receiverId, group);

        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(notificationType)
                .targetType(targetType)
                .targetId(targetId)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        //수신 동의 된 사람에게 전송 (동의 안해도 일단 저장은 한다.)
        if (enabled) {
            eventPublisher.publishEvent(new NotificationCreatedEvent(
                    savedNotification.getId(), receiverId, notificationType, targetType, targetId, sender.getNickname(), receiver.getNickname(), null, group
            ));
        }

        return savedNotification;
    }

    @Override
    public Notification createNotification(Long receiverId, Long senderId, NotificationType notificationType, TargetType targetType, Long targetId, NotificationGroup group, List<String> updatedParts) {

        String updatedPartsStr = String.join(", ", updatedParts);

        User receiver = userService.getUserByUserId(receiverId);
        User sender = userService.getUserByUserId(senderId);

        //수신 설정 되어있는지 확인
        boolean enabled = preference.isEnabled(receiverId, group);

        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(notificationType)
                .targetType(targetType)
                .targetId(targetId)
                .updatedGoalInfo(updatedPartsStr)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        //수신 동의 된 사람에게 전송 (동의 안해도 일단 저장은 한다.)
        if (enabled) {
            eventPublisher.publishEvent(new NotificationCreatedEvent(
                    savedNotification.getId(), receiverId, notificationType, targetType, targetId, sender.getNickname(), receiver.getNickname(), savedNotification.getUpdatedGoalInfo(), group
            ));
        }

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
