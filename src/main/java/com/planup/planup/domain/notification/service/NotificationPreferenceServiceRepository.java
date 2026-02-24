package com.planup.planup.domain.notification.service;

import com.planup.planup.domain.notification.entity.notification.NotificationGroup;
import com.planup.planup.domain.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceRepository {

    private final NotificationPreferenceRepository prefRepo;

    public boolean isEnabled(Long userId, NotificationGroup group) {
        // ADMIN은 기본적으로 항상 수신(정책)
        if (group == NotificationGroup.ADMIN) return true;

        return prefRepo.findByUserIdAndGroup(userId, group)
                .map(p -> p.isEnabled())
                .orElse(true);
    }
}
