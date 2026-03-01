package com.planup.planup.domain.notification.entity.device;

import com.planup.planup.domain.notification.entity.notification.NotificationGroup;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "notification_token_preference",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux_pref_user_group",
                        columnNames = {"user_id", "group_name"}
                )
        },
        indexes = {
                @Index(name = "ix_pref_user", columnList = "user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationTokenPreference {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_name", nullable = false, length = 30)
    private NotificationGroup group;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
