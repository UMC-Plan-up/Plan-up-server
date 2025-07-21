package com.planup.planup.domain.user.service;

import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserBadge;
import com.planup.planup.domain.user.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBadgeServiceImpl implements UserBadgeService {

    private final UserBadgeRepository userBadgeRepository;

    @Override
    public List<UserBadge> getUserBadgeInPeriod(User user, LocalDateTime from, LocalDateTime to) {
        return userBadgeRepository.findByUserAndCreatedAtBetween(user, from, to);
    }

    @Override
    public List<UserBadge> getTop5Recent(User user) {
        List<UserBadge> list = userBadgeRepository.findTop5ByUserOrderByCreatedAtDesc(user);
        return list;
    }
}
