package com.planup.planup.domain.notification.infra.jpa;

import com.planup.planup.domain.notification.entity.device.DeviceTokenJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataDeviceTokenJpaRepository extends JpaRepository<DeviceTokenJpa, Long> {

    Optional<DeviceTokenJpa> findByToken(String token);
    List<DeviceTokenJpa> findByUserIdAndActiveTrue(Long userId);
}
