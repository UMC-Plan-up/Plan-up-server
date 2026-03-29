package com.planup.planup.domain.notification.service.deviceToken;

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

    public void upsert(Long userId, String token, Platform platform, String appVersion, String locale, String deviceId) {
        var existing = repo.findByToken(token);
        //이미 존재한다면 기존의 토큰 사용.
        if (existing != null) {
            existing.setUserId(userId);
            existing.activate();
            existing.touch();
            repo.save(existing);
            return;
        }

        //만약 같은 아이디, 같은 디바이스에 대한 토큰이 있다면 업데이트한다.
        DeviceToken deviceToken = repo.findByUserIdAndDeviceId(userId, deviceId);
        if (deviceToken != null) {
            deviceToken.updateNewToken(token);
            repo.save(deviceToken);
            return;
        }
        repo.save(new DeviceToken(null, userId, token, platform, appVersion, locale, deviceId));
    }

    public void deactivateByToken(String token) { repo.deactivateByToken(token); }

    public void deactivateAllByUser(Long userId) { repo.deactivateAllByUserId(userId); }
}
