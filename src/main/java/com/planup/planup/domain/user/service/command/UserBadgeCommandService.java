package com.planup.planup.domain.user.service.command;

import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.user.entity.User;

public interface UserBadgeCommandService {
    boolean createUserBadge(User user, BadgeType badgeType);
}
