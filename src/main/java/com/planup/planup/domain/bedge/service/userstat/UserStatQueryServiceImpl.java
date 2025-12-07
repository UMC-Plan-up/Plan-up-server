package com.planup.planup.domain.bedge.service.userstat;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.bedge.entity.UserStat;
import com.planup.planup.domain.user.repository.UserStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserStatQueryServiceImpl {

    private final UserStatRepository userStatRepository;
    public UserStat findByUserId(Long userId) {
        Optional<UserStat> optionalUserStat = userStatRepository.findByUser_Id(userId);

        return optionalUserStat.orElseThrow(() -> new UserException(ErrorStatus._BAD_REQUEST));
    }
}
