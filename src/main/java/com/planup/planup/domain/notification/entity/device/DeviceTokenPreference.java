package com.planup.planup.domain.notification.entity.device;

import com.planup.planup.domain.notification.entity.notification.NotificationGroup;
import com.planup.planup.domain.notification.entity.notification.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "device_token_preference",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_device_token_preference_token_type",
                        columnNames = {"device_token_id", "type"}
                )
        },
        indexes = {
                @Index(name = "idx_device_pref_token", columnList = "device_token_id"),
                @Index(name = "idx_device_pref_type_enabled", columnList = "type, enabled")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeviceTokenPreference {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_token_id", nullable = false)
    private DeviceTokenJpa deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationGroup group;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
