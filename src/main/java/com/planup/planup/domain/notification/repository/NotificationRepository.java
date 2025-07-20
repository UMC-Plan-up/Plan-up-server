package com.planup.planup.domain.notification.repository;

import com.planup.planup.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 읽지 않은 알림 조회
    List<Notification> findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(Long receiverId);

    // 전체 알림 조회 (읽은 것 포함)
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
}
