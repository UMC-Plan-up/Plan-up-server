package com.planup.planup.domain.notification.entity.device;

import java.util.Collection;
import java.util.List;

public interface DeviceTokenRepository {

    DeviceToken findByToken(String token);
    List<DeviceToken> findActiveByUserId(Long userId);
    void save(DeviceToken token);
    void saveAll(Collection<DeviceToken> tokens);

    /** 토큰 비활성화(무효 처리) */
    void deactivateByToken(String token);

    /** 유저의 모든 토큰 비활성화(로그아웃/탈퇴 등 정책용) */
    void deactivateAllByUserId(Long userId);
}
