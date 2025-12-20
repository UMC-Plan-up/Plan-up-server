package com.planup.planup.domain.friend.service.policy;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.FriendException;
import com.planup.planup.domain.friend.entity.UserBlock;
import com.planup.planup.domain.friend.repository.UserBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserBlockValidator {

    private final UserBlockRepository userBlockRepository;

    public void ensureExistUserBlock(Optional<UserBlock> optionalUserBlock) {
        if (optionalUserBlock.isEmpty()) {
            throw new FriendException(ErrorStatus.NOT_EXIST_USERBLOCK);
        }
    }

    public void ensureExistUserBlock(Long userId, Long friendId) {
        //신청자와 차단 대상자가 동일한 사람인지 확인
        ensureNotSamepeople(userId, friendId);

        if (userBlockRepository.existsByBlockerIdAndBlockedId(userId, friendId)) {
            throw new FriendException(ErrorStatus.NOT_EXIST_USERBLOCK);
        }
    }

    public void ensureNotSamepeople(Long userId, Long friendId) {
        if (userId.equals(friendId))
            throw new FriendException(ErrorStatus._BAD_REQUEST);
    }
}
