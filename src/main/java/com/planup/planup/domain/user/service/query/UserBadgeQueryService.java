package com.planup.planup.domain.user.service.query;

import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.bedge.entity.UserStat;
import com.planup.planup.domain.user.dto.AuthResponseDTO;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserBadge;

import java.time.LocalDateTime;
import java.util.List;

public interface UserBadgeQueryService {
    // 뱃지 조회
    List<UserBadge> getUserBadgeInPeriod(User user, LocalDateTime from, LocalDateTime to);
    List<BadgeType> getBadgeInPeriod(User user, LocalDateTime from, LocalDateTime to);
    List<UserBadge> getTop5Recent(User user);
    List<BadgeType> getBadgeByUser(User user);

    // 통계 조회
    UserStat getUserStatByUserId(Long userId);
}
