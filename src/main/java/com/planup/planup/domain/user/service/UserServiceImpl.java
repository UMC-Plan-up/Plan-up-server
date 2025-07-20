package com.planup.planup.domain.user.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Override
    public User getUserbyUserId(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        User user = userOptional.orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));
        return user;
    }

    @Override
    public String getNickname(Long userId) {
        User user = getUserbyUserId(userId);
        return user.getNickname();
    }

    @Override
    @Transactional
    public String updateNickname(Long userId, String nickname) {
        User user = getUserbyUserId(userId);

        if (!userRepository.existsByNickname(nickname)) {
            throw new UserException(ErrorStatus.EXIST_NICKNAME);
        }
        user.setNickname(nickname);
        return user.getNickname();
    }

    @Override
    @Transactional
    public boolean updateNotificationAgree(Long userId) {
        User user = getUserbyUserId(userId);
        user.switchAlarmAllow();
        return true;
    }

    @Override
    @Transactional
    public boolean checkPassword(Long userId, String password) {
        User user = getUserbyUserId(userId);

        //TODO: password 인코딩 해야 함

        if (user.getPassword().equals(password)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String password) {
        User user = getUserbyUserId(userId);

        //TODO: password 인코딩 해야 함

        user.setPassword(user.getPassword());
    }
}
