package com.planup.planup.domain.notification.infra.jpa;

import com.planup.planup.domain.notification.entity.device.DeviceTokenJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataDeviceTokenJpaRepository extends JpaRepository<DeviceTokenJpa, Long> {

    Optional<DeviceTokenJpa> findByToken(String token);
    List<DeviceTokenJpa> findByUserIdAndActiveTrue(Long userId);
}
