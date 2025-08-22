package com.planup.planup.domain.notification.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.NotificationError;
import com.planup.planup.domain.notification.converter.NotificationConverter;
import com.planup.planup.domain.notification.dto.NotificationResponseDTO;
import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.notification.entity.NotificationType;
import com.planup.planup.domain.notification.entity.TargetType;
import com.planup.planup.domain.notification.repository.NotificationRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    //유저의 읽지 않은 알림을 시간 순 대로 가져온다
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO.NotificationDTO> getUnreadNotifications(Long receiverId) {
        User receiver = userService.getUserbyUserId(receiverId);
        List<Notification> notifications = notificationRepository.findByReceiverAndIsReadFalseOrderByCreatedAtDesc(receiver);
        return notifications.stream().map(NotificationConverter::toNotificationDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO.NotificationDTO> getUnreadNotificationsWithType(Long receiverId, String type) {
        User receiver = userService.getUserbyUserId(receiverId);
        List<Notification> notifications = notificationRepository.findByReceiverAndIsReadFalseOrderByCreatedAtDesc(receiver);
        return notifications.stream().map(NotificationConverter::toNotificationDTO).collect(Collectors.toList());
    }

    //유저의 모든 알림을 조회한다. (시간 순대로)
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO.NotificationDTO> getAllNotifications(Long receiverId) {
        User receiver = userService.getUserbyUserId(receiverId);
        return NotificationConverter.toNotificationDTOs(notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiver));
    }

    //유저가 읽었음으로 변경한다.
    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        validateReceiver(notification, userId);

        notification.markAsRead(true);
    }

    //해당 변경을 할 수 있는 권한이 있는지 확인
    private void validateReceiver(Notification notification, Long userId) {
        User user = userService.getUserbyUserId(userId);
        if (!notification.getReceiver().equals(user)) {
            throw new NotificationError(ErrorStatus.UNAUTHORIZED_NOTIFICATION_ACCESS);
        }
    }

    //유저에 따라 가장 최근의 5개 알림을 반환한다. (읽음 여부와 상관없이)
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO.NotificationDTO> getTop5RecentByUser(Long userId) {
        User receiver = userService.getUserbyUserId(userId);
        List<Notification> notificationList = notificationRepository.findTop3ByReceiverOrderByCreatedAtDesc(receiver);
        return NotificationConverter.toNotificationDTOs(notificationList);
    }

    //새로운 알림을 만든다.
    @Override
    @Transactional
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

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
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

        return notificationRepository.save(notification);
    }

}
