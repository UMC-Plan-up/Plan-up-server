package com.planup.planup.domain.bedge.service;

import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.bedge.repository.BadgeRepository;
import com.planup.planup.domain.user.entity.UserBadge;
import com.planup.planup.domain.user.entity.UserStat;
import com.planup.planup.domain.user.service.UserBadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BadgeServiceImpl implements BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeService userBadgeService;

    public boolean isEligibleForEarlyInfluencerBadge(UserStat userStat) {
        LocalDateTime createdAt = userStat.getUser().getCreatedAt();
        if (createdAt.plusDays(3).isBefore(LocalDateTime.now())) {
            userBadgeService.createUserBadge(userStat.getUser(), BadgeType.INFLUENTIAL_STARTER);
            return true;
        }
        return false;
    }

}
