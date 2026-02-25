package com.planup.planup.domain.user.entity;

import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "user_badge",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_badge_user_badge_type",
                        columnNames = {"user_id", "badge_type"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_user_badge_user_id",
                        columnList = "user_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class UserBadge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저와 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeType badgeType;

    public void setUser(User user) {
        this.user = user;
        user.getUserBadges().add(this);
    }
}
