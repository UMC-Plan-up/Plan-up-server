package com.planup.planup.domain.notification.infra.jpa;

import com.planup.planup.domain.notification.entity.device.DeviceToken;
import com.planup.planup.domain.notification.entity.device.DeviceTokenJpa;
import com.planup.planup.domain.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaDeviceTokenRepository implements DeviceTokenRepository {

    private final SpringDataDeviceTokenJpaRepository jpa;

    @Override
    public DeviceToken findByToken(String token) {
        return jpa.findByToken(token).map(JpaMapper::toDomain).orElse(null);
    }

    @Override
    public List<DeviceToken> findActiveByUserId(Long userId) {
        return jpa.findByUserIdAndActiveTrue(userId).stream().map(JpaMapper::toDomain).toList();
    }

    @Override @Transactional
    public void save(DeviceToken dt) {
        jpa.save(JpaMapper.toJpa(dt));
    }

    @Override @Transactional
    public void saveAll(Collection<DeviceToken> tokens) {
        jpa.saveAll(tokens.stream().map(JpaMapper::toJpa).toList());
    }

    @Override @Transactional
    public void deactivateByToken(String token) {
        jpa.findByToken(token).ifPresent(DeviceTokenJpa::deactivate);
    }

    @Override @Transactional
    public void deactivateAllByUserId(Long userId) {
        jpa.findByUserIdAndActiveTrue(userId).forEach(DeviceTokenJpa::deactivate);
    }

    @Override @Transactional
    public void activateByToken(String token) {
        jpa.findByToken(token).ifPresent(DeviceTokenJpa::activate);
    }

    @Override @Transactional
    public void activateAllByUserId(Long userId) {
        jpa.findByUserIdAndActiveTrue(userId).forEach(DeviceTokenJpa::activate);
    }

    static class JpaMapper {
        static DeviceToken toDomain(DeviceTokenJpa e) {
            var d = new DeviceToken(e.getUserId(), e.getToken(), e.getPlatform(), e.getAppVersion(), e.getLocale());
            // id/active/시간 등 동기화
            try { var idField = DeviceToken.class.getDeclaredField("id"); idField.setAccessible(true); idField.set(d, e.getId()); } catch (Exception ignored) {}
            d.touch(); // 간단히 업데이트 표시(필요시 정확 매핑)
            if (!e.isActive()) d.deactivate();
            return d;
        }
        static DeviceTokenJpa toJpa(DeviceToken d) {
            return DeviceTokenJpa.builder()
                    .userId(d.getUserId())
                    .token(d.getToken())
                    .platform(d.getPlatform())
                    .appVersion(d.getAppVersion())
                    .locale(d.getLocale())
                    .active(true)
                    .lastSeenAt(Instant.now())
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        }
    }
}