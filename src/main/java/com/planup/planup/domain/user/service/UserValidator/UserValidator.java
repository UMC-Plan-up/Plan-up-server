package com.planup.planup.domain.user.service.UserValidator;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public boolean existUserId(Long userId) {

        if (userRepository.existsById(userId)) {
            return true;
        }

        throw new UserException(ErrorStatus.NOT_FOUND_USER);
    }
}
