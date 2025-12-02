package com.planup.planup.domain.user.service.command;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.BadgeException;
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

        if (user == null || badge == null) {
            throw new BadgeException(ErrorStatus.INVALID_BADGE_TYPE);
        }

        boolean alreadyHasBadge = userBadgeRepository.existsByUserAndBadgeType(user, badge);

        if (alreadyHasBadge) {
            return false; // 이미 보유 중이면 false 반환
        }

        UserBadge userBadge = userBadgeConverter.toUserBadgeEntity(user, badge);
        userBadgeRepository.save(userBadge);

        return true;
    }
}