package com.planup.planup.domain.user.service.command;

import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserBadge;
import com.planup.planup.domain.user.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserBadgeCommandServiceImpl implements UserBadgeCommandService {

    private final UserBadgeRepository userBadgeRepository;

    @Override
    public boolean createUserBadge(User user, BadgeType badge) {
        List<UserBadge> isExist = userBadgeRepository.findByUserAndBadgeType(user, badge);

        if (!isExist.isEmpty()) {
            return false;
        }

        userBadgeRepository.save(UserBadge.builder()
                .user(user)
                .badgeType(badge)
                .build());

        return true;
    }
}