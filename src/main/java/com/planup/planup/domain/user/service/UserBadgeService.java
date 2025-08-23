package com.planup.planup.domain.user.service;

import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserBadge;

import java.time.LocalDateTime;
import java.util.List;

public interface UserBadgeService {


    boolean createUserBadge(User user, BadgeType badgeType);

    List<UserBadge> getUserBadgeInPeriod(User user, LocalDateTime from, LocalDateTime to);

    List<BadgeType> getBadgeInPeriod(User user, LocalDateTime from, LocalDateTime to);

    List<UserBadge> getTop5Recent(User user);

    List<BadgeType> getBadgeByUser(User user);

}
