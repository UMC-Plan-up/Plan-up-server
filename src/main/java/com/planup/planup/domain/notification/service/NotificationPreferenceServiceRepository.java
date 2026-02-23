package com.planup.planup.domain.notification.service;

@Service
@RequiredArgsConstructor
public class DeviceTokenPreferenceServiceRepository {

    private final NotificationPreferenceRepository prefRepo;

    public boolean isEnabled(Long userId, NotificationGroup group) {
        // ADMIN은 기본적으로 항상 수신(정책)
        if (group == NotificationGroup.ADMIN) return true;

        return prefRepo.findByUserIdAndGroup(userId, group)
                .map(p -> p.isEnabled())
                .orElse(true);
    }
}
