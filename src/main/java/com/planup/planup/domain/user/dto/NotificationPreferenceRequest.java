package com.planup.planup.domain.user.dto;

public record NotificationPreferenceRequest(
        Long termsId,
        boolean enabled
) {}
