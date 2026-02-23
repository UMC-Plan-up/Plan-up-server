package com.planup.planup.domain.notification.entity.device;

import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 비지니스 로직에서 사용하기 위해서 만든 DeviceToken 관련 클래스
 */

@Getter
public class DeviceToken {

    //jpa entity의 id
    private Long id;

    private Long userId;
    private String token;
    private Platform platform;     // ANDROID/IOS/WEB 등
    private String appVersion;
    private String locale;          //사용자 (언어 - 국가) 정보
    private boolean active = true;

    private String deviceId;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // 생성자/게터/세터
    public DeviceToken(Long id, Long userId, String token, Platform platform, String appVersion, String locale, String deviceId) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.platform = platform;
        this.appVersion = appVersion;
        this.locale = locale;
        this.deviceId = deviceId;
    }

    public void touch() {
        LocalDateTime lastSeenAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateNewToken(String token) {
        activate();
        this.token = token;
    }

    public void setUserId(Long id) {
        this.userId = id;
    }
}
