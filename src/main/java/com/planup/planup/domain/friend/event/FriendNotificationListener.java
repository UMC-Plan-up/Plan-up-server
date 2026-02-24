package com.planup.planup.domain.friend.event;

import com.planup.planup.domain.friend.event.dto.FriendRejectSentEvent;
import com.planup.planup.domain.friend.event.dto.FriendRequestAcceptedEvent;
import com.planup.planup.domain.friend.event.dto.FriendRequestSentEvent;
import com.planup.planup.domain.notification.entity.notification.NotificationGroup;
import com.planup.planup.domain.notification.entity.notification.NotificationType;
import com.planup.planup.domain.notification.entity.notification.TargetType;
import com.planup.planup.domain.notification.service.notification.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FriendNotificationListener {

    private final NotificationCommandService notificationService;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFriendRequestSent(FriendRequestSentEvent e) {
        notificationService.createNotification(
                e.receiverId(), e.senderId(),
                NotificationType.FRIEND_REQUEST_SENT, TargetType.USER, e.senderId(), NotificationGroup.FRIEND);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFriendRequestAccepted(FriendRequestAcceptedEvent e) {
        notificationService.createNotification(
                e.senderId(), e.receiverId(),
                NotificationType.FRIEND_REQUEST_ACCEPTED, TargetType.USER, e.receiverId(), NotificationGroup.FRIEND);

        notificationService.createNotification(
                e.receiverId(), e.senderId(),
                NotificationType.FRIEND_REQUEST_ACCEPTED, TargetType.USER, e.senderId(), NotificationGroup.FRIEND);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFriendRequestSent(FriendRejectSentEvent e) {
        notificationService.createNotification(
                e.receiverId(), e.senderId(),
                NotificationType.FRIEND_REQUEST_SENT, TargetType.USER, e.senderId(), NotificationGroup.FRIEND);
    }
}
