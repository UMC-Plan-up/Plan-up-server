package com.planup.planup.domain.user.entity;

import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_badge")
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

    // 뱃지와 매핑
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "badge_id", nullable = false)
//    private Badge badge;

    @Enumerated(EnumType.STRING)
    private BadgeType badgeType;

    public void setUser(User user) {
        this.user = user;
        user.getUserBadges().add(this);
    }
}
