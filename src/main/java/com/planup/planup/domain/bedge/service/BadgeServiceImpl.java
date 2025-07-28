package com.planup.planup.domain.bedge.service;

import com.planup.planup.domain.bedge.entity.Badge;
import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.bedge.repository.BadgeRepository;
import com.planup.planup.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BadgeServiceImpl implements BadgeService {

    private final BadgeRepository badgeRepository;

    public void createBadge(User user, BadgeType type) {
        return badgeRepository.save(new Badge.builder())
    }
}
