package com.planup.planup.domain.notification.entity.device;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DeviceTokenRepository extends JpaRepository<DeviceTokenJpa, Long> {

    DeviceTokenJpa findByToken(String token);
    List<DeviceTokenJpa> findActiveByUserId(Long userId);
    void save(DeviceTokenJpa token);
    void saveAll(Collection<DeviceTokenJpa> tokens);

    /** 토큰 비활성화(무효 처리) */
    void deactivateByToken(String token);

    /** 유저의 모든 토큰 비활성화(로그아웃/탈퇴 등 정책용) */
    void deactivateAllByUserId(Long userId);

    /** 토큰 활성화 */
    void activateByToken(String token);

    /** 유저의 모든 토큰 활성화 */
    void activateAllByUserId(Long userId);
}
