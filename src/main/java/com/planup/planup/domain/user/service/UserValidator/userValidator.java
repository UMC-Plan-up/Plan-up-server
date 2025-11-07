package com.planup.planup.domain.user.service.UserValidator;

import com.planup.planup.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class userValidator {

    private final UserRepository userRepository;

    public boolean existUserId(Long userId) {
        return userRepository.existsById(userId);
    }
}
