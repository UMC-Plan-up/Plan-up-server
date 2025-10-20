package com.planup.planup.domain.notification.entity.device;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "device_token", indexes = {
        @Index(name = "ix_device_token_user", columnList = "userId"),
        @Index(name = "ix_device_token_active", columnList = "active")
}, uniqueConstraints = @UniqueConstraint(name = "ux_device_token_token", columnNames = "token"))
public class DeviceToken {

    @Id
    @GeneratedValue
    private Long id;

    private Long userId;

    @Column(nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    private Platform platform;     // ANDROID/IOS/WEB 등

    private String appVersion;
    private String locale;

    private boolean active = true;
    private Instant lastSeenAt = Instant.now();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public DeviceToken() {};

    public DeviceToken(Long userId, String token, Platform platform, String appVersion, String locale) {
        this.userId = userId;
        this.token = token;
        this.platform = platform;
        this.appVersion = appVersion;
        this.locale = locale;
    }

    public void touch() {
        this.lastSeenAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    // 기본 생성자/게터/세터 생략(롬복 @Data/@Builder 등을 써도 됨)
}
