package com.planup.planup.domain.notification.service;

import com.planup.planup.domain.notification.entity.device.NotificationTokenPreference;
import com.planup.planup.domain.notification.entity.notification.NotificationGroup;
import com.planup.planup.domain.notification.repository.NotificationPreferenceRepository;
import com.planup.planup.domain.user.entity.Terms;
import com.planup.planup.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository prefRepo;

    //약관별 아이디 값
    private static final long SERVICE_TERMS_ID = 4L;
    private static final long MARKETING_TERMS_ID = 5L;

    public boolean isEnabled(Long userId, NotificationGroup group) {
        // ADMIN은 기본적으로 항상 수신(정책)
        if (group == NotificationGroup.ADMIN) return true;

        return prefRepo.findByUserIdAndGroup(userId, group)
                .map(p -> p.isEnabled())
                .orElse(false);
    }

    private void createNotificationPreference(User user, NotificationGroup group, boolean enable) {

        if (!prefRepo.existsByUserIdAndGroup(user.getId(), group)) {
            createNotificationPreference(user, group, true);
        }

        NotificationTokenPreference NP = NotificationTokenPreference.builder()
                .userId(user.getId())
                .group(group)
                .enabled(enable)
                .build();
        NotificationTokenPreference save = prefRepo.save(NP);
        user.addPreferences(save);
    }

    public void addNotificationPreference(List<Terms> terms, User user) {

        Set<Long> agreedTermIds = terms.stream()
                .map(Terms::getId)
                .collect(Collectors.toSet());

        if (agreedTermIds.contains(SERVICE_TERMS_ID)) {
            createNotificationPreference(user, NotificationGroup.SERVICE, true);
        }

        if (agreedTermIds.contains(MARKETING_TERMS_ID)) {
            createNotificationPreference(user, NotificationGroup.MARKETING, true);
        }
    }
}
