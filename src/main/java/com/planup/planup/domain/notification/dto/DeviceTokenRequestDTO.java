package com.planup.planup.domain.notification.dto;

import com.planup.planup.domain.notification.entity.device.Platform;

public record DeviceTokenRequestDTO(
        String token,
        Platform platform,
        String appVersion,
        String local,

        String deviceId
) {
}
