package com.planup.planup.domain.user.entity;

import com.planup.planup.domain.bedge.entity.Badge;
import com.planup.planup.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_badge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserBadge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저와 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 뱃지와 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    public void setUser(User user) {
        this.user = user;
        user.getUserBadges().add(this);
    }

    public void setBadge(Badge badge) {
        this.badge = badge;
        badge.getUserBadges().add(this);
    }
}
