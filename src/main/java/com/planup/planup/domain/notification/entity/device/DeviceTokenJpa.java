package com.planup.planup.domain.notification.entity.device;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 토큰 관련 정보를 실재로 데이터 베이스에 저장하기 위해 사용하는 클래스
 *
 * 비지니스 로직에선 사용하지 않는다.
 * Spring Data Jpa를 사용하지 않게 되더라도 비지니스 로직에 영향이 없도록
 */

@Entity
@Table(name = "device_token", indexes = {
        @Index(name = "ix_device_token_user", columnList = "userId"),
        @Index(name = "ix_device_token_active", columnList = "active")
}, uniqueConstraints = @UniqueConstraint(name = "ux_device_token_token", columnNames = "token"))
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceTokenJpa {

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

    public DeviceTokenJpa(Long userId, String token, Platform platform, String appVersion, String locale) {
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

    public void activate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }
}
