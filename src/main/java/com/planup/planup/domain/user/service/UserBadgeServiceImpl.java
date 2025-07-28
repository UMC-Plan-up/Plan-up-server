package com.planup.planup.domain.user.service;

import com.planup.planup.domain.bedge.entity.Badge;
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
    public UserBadge createUserBadge(User user, Badge badge) {
        return userBadgeRepository.save(UserBadge.builder()
                .user(user)
                .badge(badge)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBadge> getUserBadgeInPeriod(User user, LocalDateTime from, LocalDateTime to) {
        return userBadgeRepository.findByUserAndCreatedAtBetween(user, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Badge> getBadgeInPeriod(User user, LocalDateTime from, LocalDateTime to) {
        return userBadgeRepository.findByUserAndCreatedAtBetween(user, from, to).stream().map(UserBadge::getBadge).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBadge> getTop5Recent(User user) {
        List<UserBadge> list = userBadgeRepository.findTop5ByUserOrderByCreatedAtDesc(user);
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Badge> getBadgeByUser(User user) {
        List<UserBadge> userbadges = userBadgeRepository.findByUserOrderByCreatedAtDesc(user);
        return userbadges.stream().map(UserBadge::getBadge).collect(Collectors.toList());
    }
}
