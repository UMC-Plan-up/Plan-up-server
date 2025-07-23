package com.planup.planup.domain.user.service;

import com.planup.planup.domain.bedge.entity.Badge;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserBadge;
import com.planup.planup.domain.user.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserBadgeServiceImpl implements UserBadgeService {

    private final UserBadgeRepository userBadgeRepository;

    @Override
    public List<UserBadge> getUserBadgeInPeriod(User user, LocalDateTime from, LocalDateTime to) {
        return userBadgeRepository.findByUserAndCreatedAtBetween(user, from, to);
    }

    @Override
    public List<Badge> getBadgeInPeriod(User user, LocalDateTime from, LocalDateTime to) {
        return userBadgeRepository.findByUserAndCreatedAtBetween(user, from, to).stream().map(UserBadge::getBadge).collect(Collectors.toList());
    }

    @Override
    public List<UserBadge> getTop5Recent(User user) {
        List<UserBadge> list = userBadgeRepository.findTop5ByUserOrderByCreatedAtDesc(user);
        return list;
    }

    @Override
    public List<Badge> getBadgeByUser(User user) {
        List<UserBadge> userbadges = userBadgeRepository.findByUserOrderByCreatedAtDesc(user);
        return userbadges.stream().map(UserBadge::getBadge).collect(Collectors.toList());
    }
}
