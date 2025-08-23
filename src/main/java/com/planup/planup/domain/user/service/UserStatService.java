package com.planup.planup.domain.user.service;

import com.planup.planup.domain.bedge.entity.UserStat;

public interface UserStatService {
    UserStat getUserStatByUserId(Long userId);
}
