package com.planup.planup.domain.user.service;

import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserBadge;
import com.planup.planup.domain.user.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserBadgeServiceImpl implements UserBadgeService {

    private final UserBadgeRepository userBadgeRepository;

    @Override
    @Transactional
    public UserBadge createUserBadge(User user, BadgeType badge) {

        List<UserBadge> isExist = userBadgeRepository.findByUserAndBadgeType(user, badge);
        if (isExist.size() != 0) {
            return isExist.get(0);
        }

        return userBadgeRepository.save(UserBadge.builder()
                .user(user)
                .badgeType(badge)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBadge> getUserBadgeInPeriod(User user, LocalDateTime from, LocalDateTime to) {
        return userBadgeRepository.findByUserAndCreatedAtBetween(user, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BadgeType> getBadgeInPeriod(User user, LocalDateTime from, LocalDateTime to) {
        return userBadgeRepository.findByUserAndCreatedAtBetween(user, from, to).stream().map(UserBadge::getBadgeType).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBadge> getTop5Recent(User user) {
        List<UserBadge> list = userBadgeRepository.findTop5ByUserOrderByCreatedAtDesc(user);
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BadgeType> getBadgeByUser(User user) {
        List<UserBadge> userbadges = userBadgeRepository.findByUserOrderByCreatedAtDesc(user);
        return userbadges.stream().map(UserBadge::getBadgeType).collect(Collectors.toList());
    }
}
