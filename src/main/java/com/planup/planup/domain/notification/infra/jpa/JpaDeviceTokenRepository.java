package com.planup.planup.domain.notification.infra.jpa;

import com.planup.planup.domain.notification.entity.device.DeviceToken;
import com.planup.planup.domain.notification.entity.device.DeviceTokenJpa;
import com.planup.planup.domain.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Override
    public DeviceToken findByUserIdAndDeviceId(Long userId, String deviceId) {
        return jpa.findByUserIdAndDeviceIdAndActiveTrue(userId, deviceId).map(JpaMapper::toDomain).orElse(null);
    }

    @Override @Transactional
    public void save(DeviceToken dt) {
        DeviceTokenJpa entity;

        //dt에 id 값이 있다 -> 기존에 데이터베이스에 저장된 값이다.
        if (dt.getId() != null) {
            entity = jpa.findById(dt.getId())
                    .orElseThrow(() -> new IllegalStateException("DeviceTokenJpa not found: " + dt.getId()));
        } else {
            //기존에 데이터베이스에 저장되지 않은 엔티티라면 token을 기준으로 검색, 없으면 새로운 객체
            entity = jpa.findByToken(dt.getToken()).orElse(null);

            if (entity == null) {
                entity = new DeviceTokenJpa();
            }
        }

        // 여기서 entity에 dt 값을 반영 (update)
        entity.updateInfo(
                dt.getUserId(),
                dt.getToken(),
                dt.getPlatform(),
                dt.getAppVersion(),
                dt.getLocale(),
                dt.getDeviceId(),
                dt.isActive()
        );

        jpa.save(entity);
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
            var d = new DeviceToken(e.getId(), e.getUserId(), e.getToken(), e.getPlatform(), e.getAppVersion(), e.getLocale(), e.getDeviceId());
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
                    .lastSeenAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
    }
}