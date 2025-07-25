package com.planup.planup.domain.notification.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.NotificationError;
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

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    //유저의 읽지 않은 알림을 시간 순 대로 가져온다
    @Override
    public List<Notification> getUnreadNotifications(Long receiverId) {
        User receiver = userService.getUserbyUserId(receiverId);
        return notificationRepository.findByReceiverAndIsReadFalseOrderByCreatedAtDesc(receiver);
    }

    //유저의 모든 알림을 조회한다. (시간 순대로)
    @Override
    public List<Notification> getAllNotifications(Long receiverId) {
        User receiver = userService.getUserbyUserId(receiverId);
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiver);
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
    public List<Notification> getTop5RecentByUser(Long userId) {
        User receiver = userService.getUserbyUserId(userId);
        return notificationRepository.findTop3ByReceiverOrderByCreatedAtDesc(receiver);
    }

    //새로운 알림을 만든다.
    @Override
    public Notification createNotification(Long receiverId, Long senderId, NotificationType notificationType, TargetType targetType, Long targetId) {

        User receiver = userService.getUserbyUserId(receiverId);
        User sender = userService.getUserbyUserId(senderId);

        return Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type(notificationType)
                .targetType(targetType)
                .targetId(targetId)
                .build();
    }
}
