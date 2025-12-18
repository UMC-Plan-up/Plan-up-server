package com.planup.planup.domain.user.converter;

import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserBadge;
import org.springframework.stereotype.Component;

@Component
public class UserBadgeConverter {

    /**
     * UserBadge 엔티티 생성
     */
    public UserBadge toUserBadgeEntity(User user, BadgeType badgeType) {
        return UserBadge.builder()
                .user(user)
                .badgeType(badgeType)
                .build();
    }
}
