package com.planup.planup.domain.notification.repository;

import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.notification.entity.NotificationType;
import com.planup.planup.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 읽지 않은 알림 조회
    List<Notification> findByReceiverAndIsReadFalseOrderByCreatedAtDesc(User receiver);

    // 전체 알림 조회 (읽은 것 포함)
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(User receiver);

    //최근 3개의 알림 - 유저별
    List<Notification> findTop3ByReceiverOrderByCreatedAtDesc(User receiver);

    @Query("""
      SELECT n
      FROM Notification n
      WHERE n.receiver = :receiver
        AND n.isRead = false
      ORDER BY n.createdAt DESC
""")
    List<Notification> findUnreadByReceiverAndType(
            @Param("receiver") User receiver
    );
}
