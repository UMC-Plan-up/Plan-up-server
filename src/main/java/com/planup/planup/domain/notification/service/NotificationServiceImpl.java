package com.planup.planup.domain.notification.service;

import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.notification.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    //유저의 읽지 않은 알림을 시간 순 대로 가져온다
    @Override
    public List<Notification> getUnreadNotifications(Long receiverId) {
        return notificationRepository.findByReceiverIdAndIsReadFalseIdOrderByCreatedAtDesc(receiverId);
    }

    //유저의 모든 알림을 조회한다. (시간 순대로)
    @Override
    public List<Notification> getAllNotifications(Long receiverId) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification); // JPA dirty checking or merge
    }
}
