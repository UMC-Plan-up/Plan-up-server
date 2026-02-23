package com.planup.planup.domain.notification.entity.device;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 토큰 관련 정보를 실재로 데이터 베이스에 저장하기 위해 사용하는 클래스
 *
 * 비지니스 로직에선 사용하지 않는다.
 * Spring Data Jpa를 사용하지 않게 되더라도 비지니스 로직에 영향이 없도록
 */

@Entity
@Table(name = "device_token", indexes = {
        @Index(name = "ix_device_token_user", columnList = "userId, deviceId"),
        @Index(name = "ix_device_token_active", columnList = "active")
}, uniqueConstraints =
        @UniqueConstraint(name = "ux_device_token_token", columnNames = "token"))
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceTokenJpa extends BaseTimeEntity {

    @Id
    @GeneratedValue
    private Long id;

    private Long userId;

    @Column(nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    private Platform platform;     // ANDROID/IOS/WEB 등

    @OneToMany(mappedBy = "deviceToken", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeviceTokenPreference> preferences = new ArrayList<>();

    private String appVersion;
    private String locale;

    private String deviceId;

    private boolean active = true;
    private LocalDateTime lastSeenAt = LocalDateTime.now();

    public DeviceTokenJpa(Long userId, String token, Platform platform, String appVersion, String locale, String deviceId) {
        this.userId = userId;
        this.token = token;
        this.platform = platform;
        this.appVersion = appVersion;
        this.locale = locale;
        this.deviceId = deviceId;
    }

    public void touch() {
        this.lastSeenAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
        this.lastSeenAt = LocalDateTime.now();
    }

    public void updateAppInfo(Platform platform, String appVersion, String locale) {
        this.platform = platform;
        this.appVersion = appVersion;
        this.locale = locale;
        touch();
    }

    public void updateInfo(
            Long userId,
            String token,
            Platform platform,
            String appVersion,
            String locale,
            String deviceId,
            boolean active
    ) {
        this.userId = userId;
        this.token = token;
        this.platform = platform;
        this.appVersion = appVersion;
        this.locale = locale;
        this.deviceId = deviceId;
        this.active = active;
        this.lastSeenAt = LocalDateTime.now();
    }
}
