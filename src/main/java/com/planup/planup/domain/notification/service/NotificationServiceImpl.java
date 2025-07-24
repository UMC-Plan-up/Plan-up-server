package com.planup.planup.domain.notification.service;

import com.planup.planup.domain.notification.entity.Notification;
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

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        notification.markAsRead(true);
    }

    @Override
    public List<Notification> getTop5RecentByUser(Long userId) {
        User receiver = userService.getUserbyUserId(userId);
        return notificationRepository.findTop3ByReceiverOrderByCreatedAtDesc(receiver);
    }
}
