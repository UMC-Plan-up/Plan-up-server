package com.planup.planup.domain.user.service.command;

import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.user.converter.UserBadgeConverter;
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
    private final UserBadgeConverter userBadgeConverter;

    @Override
    public boolean createUserBadge(User user, BadgeType badge) {
        List<UserBadge> isExist = userBadgeRepository.findByUserAndBadgeType(user, badge);

        if (!isExist.isEmpty()) {
            return false;
        }

        UserBadge userBadge = userBadgeConverter.toUserBadgeEntity(user, badge);
        userBadgeRepository.save(userBadge);

        return true;
    }
}