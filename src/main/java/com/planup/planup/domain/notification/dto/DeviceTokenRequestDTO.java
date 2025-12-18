package com.planup.planup.domain.notification.dto;

import com.planup.planup.domain.notification.entity.device.Platform;

public record DeviceTokenRequestDTO(
        String token,
        Platform plateFrom,
        String appVersion,
        String local
) {
}
