package com.planup.planup.domain.notification.service.deviceTokenService;

import com.planup.planup.domain.notification.entity.device.DeviceToken;
import com.planup.planup.domain.notification.repository.DeviceTokenRepository;
import com.planup.planup.domain.notification.entity.device.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeviceTokenService {

    private final DeviceTokenRepository repo;

    public void upsert(Long userId, String token, Platform platform, String appVersion, String locale) {
        var existing = repo.findByToken(token);
        if (existing != null) {
            existing.setUserId(userId);
            existing.activate();
            existing.touch();
            repo.save(existing);
            return;
        }
        repo.save(new DeviceToken(userId, token, platform, appVersion, locale));
    }

    public void deactivateByToken(String token) { repo.deactivateByToken(token); }

    public void deactivateAllByUser(Long userId) { repo.deactivateAllByUserId(userId); }
}
