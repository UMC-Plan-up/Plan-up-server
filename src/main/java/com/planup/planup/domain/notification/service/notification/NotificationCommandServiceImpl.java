package com.planup.planup.domain.notification.service.notification;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.NotificationError;
import com.planup.planup.domain.notification.entity.notification.Notification;
import com.planup.planup.domain.notification.entity.notification.NotificationGroup;
import com.planup.planup.domain.notification.entity.notification.NotificationType;
import com.planup.planup.domain.notification.entity.notification.TargetType;
import com.planup.planup.domain.notification.dto.NotificationReadRequest;
import com.planup.planup.domain.notification.entity.device.NotificationCreatedEvent;
import com.planup.planup.domain.notification.repository.NotificationRepository;
import com.planup.planup.domain.notification.service.NotificationPreferenceService;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.query.UserQueryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final UserQueryService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationPreferenceService preference;

    //새로운 알림을 만든다.

    @Override
    public Notification createNotification(Long receiverId, Long senderId, NotificationType notificationType,
                                           TargetType targetType, Long targetId, NotificationGroup group) {

        log.info("[NotificationCreate] start - receiverId={}, senderId={}, type={}, targetType={}, targetId={}, group={}",
                receiverId, senderId, notificationType, targetType, targetId, group);

        User receiver = userService.getUserByUserId(receiverId);
        User sender = userService.getUserByUserId(senderId);

        boolean enabled = preference.isEnabled(receiverId, group);
        log.info("[NotificationCreate] preference - receiverId={}, enabled={}", receiverId, enabled);

        Notification notification = Notification.create(sender, receiver, group, notificationType, targetType, targetId);
        Notification savedNotification = notificationRepository.save(notification);

        log.info("[NotificationCreate] saved - notificationId={}", savedNotification.getId());

        if (enabled) {
            log.info("[NotificationCreate] event publish - notificationId={}, receiverId={}",
                    savedNotification.getId(), receiverId);

            eventPublisher.publishEvent(new NotificationCreatedEvent(
                    savedNotification.getId(), receiverId, notificationType, targetType, targetId,
                    sender.getNickname(), senderId, sender.getProfileImg(),
                    receiver.getNickname(), null, group
            ));
        } else {
            log.info("[NotificationCreate] skip push (disabled) - receiverId={}", receiverId);
        }

        return savedNotification;
    }

    @Override
    public Notification createNotification(Long receiverId, Long senderId, NotificationType notificationType,
                                           TargetType targetType, Long targetId, NotificationGroup group,
                                           List<String> updatedParts) {

        log.info("[NotificationCreateWithParts] start - receiverId={}, senderId={}, updatedParts={}",
                receiverId, senderId, updatedParts);

        String updatedPartsStr = String.join(", ", updatedParts);

        User receiver = userService.getUserByUserId(receiverId);
        User sender = userService.getUserByUserId(senderId);

        boolean enabled = preference.isEnabled(receiverId, group);
        log.info("[NotificationCreateWithParts] preference - receiverId={}, enabled={}", receiverId, enabled);

        Notification notification = Notification.create(sender, receiver, group, notificationType, targetType, targetId);
        Notification savedNotification = notificationRepository.save(notification);

        log.info("[NotificationCreateWithParts] saved - notificationId={}", savedNotification.getId());

        if (enabled) {
            log.info("[NotificationCreateWithParts] event publish - notificationId={}", savedNotification.getId());

            eventPublisher.publishEvent(new NotificationCreatedEvent(
                    savedNotification.getId(), receiverId, notificationType, targetType, targetId,
                    sender.getNickname(), senderId, sender.getProfileImg(),
                    receiver.getNickname(), savedNotification.getUpdatedGoalInfo(), group
            ));
        } else {
            log.info("[NotificationCreateWithParts] skip push (disabled) - receiverId={}", receiverId);
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

    @Override
    public void markAsRead(NotificationReadRequest request, Long userId) {
        for (Long notificationId : request.notificationIdList()) {
            markAsRead(notificationId, userId);
        }
    }

    //해당 변경을 할 수 있는 권한이 있는지 확인
    private void validateReceiver(Notification notification, Long userId) {
        User user = userService.getUserByUserId(userId);
        if (!notification.getReceiver().equals(user)) {
            throw new NotificationError(ErrorStatus.UNAUTHORIZED_NOTIFICATION_ACCESS);
        }
    }
}
