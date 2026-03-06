package com.planup.planup.domain.notification.repository;

import com.planup.planup.domain.notification.entity.device.NotificationTokenPreference;
import com.planup.planup.domain.notification.entity.notification.NotificationGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationTokenPreference, Long> {
    Optional<NotificationTokenPreference> findByUserIdAndGroup(Long userId, NotificationGroup group);

    boolean existsByUserIdAndGroup(Long userId, NotificationGroup group);
}
