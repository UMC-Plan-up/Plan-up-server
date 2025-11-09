package com.planup.planup.domain.user.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.bedge.entity.UserStat;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.domain.user.repository.UserStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStatServiceImpl implements UserStatService {
    private final UserStatRepository userStatRepository;
    private final UserService userService;
    @Override
    public UserStat getUserStatByUserId(Long userId) {
        User user = userService.getUserByUserId(userId);

        UserStat userStat = userStatRepository.findByUser(user);

        return userStat;
    }

}
