package com.planup.planup.domain.notification.repository;

import com.planup.planup.domain.notification.entity.device.DeviceTokenJpa;
import com.planup.planup.domain.notification.entity.device.DeviceTokenPreference;
import com.planup.planup.domain.notification.entity.notification.NotificationGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface DeviceTokenPreferenceRepository extends JpaRepository<DeviceTokenPreference, Long> {
    Optional<DeviceTokenPreference> findByDeviceTokenAndGroup(DeviceTokenJpa token, NotificationGroup group);
}
