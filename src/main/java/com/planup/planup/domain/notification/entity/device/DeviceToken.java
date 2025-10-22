package com.planup.planup.domain.notification.entity.device;

import lombok.Getter;

import java.time.Instant;

/**
 * 비지니스 로직에서 사용하기 위해서 만든 DeviceToken 관련 클래스
 */

@Getter
public class DeviceToken {

    private Long userId;
    private String token;
    private Platform platform;     // ANDROID/IOS/WEB 등
    private String appVersion;
    private String locale;
    private boolean active = true;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    // 생성자/게터/세터
    public DeviceToken(Long userId, String token, Platform platform, String appVersion, String locale) {
        this.userId = userId;
        this.token = token;
        this.platform = platform;
        this.appVersion = appVersion;
        this.locale = locale;
    }

    public void touch() {
        Instant lastSeenAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public void setUserId(Long id) {
        this.userId = id;
    }
}
