package com.planup.planup.domain.friend.service.policy;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.FriendException;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FriendValidator {

    private final FriendRepository friendRepository;

    public void ensureNotAlreadyFriend(Long userId, Long friendId) {
        if (friendRepository.existsByUsersAndStatus(userId, friendId, FriendStatus.ACCEPTED)) {
            throw new FriendException(ErrorStatus.ALREADY_FRIEND);
        }
    }

    public void ensureNotAlreadyRequested(Long userId, Long friendId) {
        if (friendRepository.existsByUsersAndStatus(userId, friendId, FriendStatus.REQUESTED)) {
            throw new FriendException(ErrorStatus.ALREADY_REQUESTED);
        }
    }

    public void ensureNotBlocked(Long userId, Long friendId) {
        if (friendRepository.existsByUsersAndStatus(userId, friendId, FriendStatus.BLOCKED)) {
            throw new FriendException(ErrorStatus.FRIEND_BLOCKED);
        }
    }

    public void ensureNotSelfRequest(Long userId, Long friendId) {
        if (userId == friendId) throw new FriendException(ErrorStatus.SAME_USER);
    }
}
