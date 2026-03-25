package com.planup.planup.domain.friend.service.policy;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.FriendException;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.bedge.entity.UserStat;
import com.planup.planup.domain.bedge.service.userstat.UserStatQueryServiceImpl;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.friend.repository.UserBlockRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.enums.UserActivate;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.domain.user.repository.UserStatRepository;
import com.planup.planup.domain.user.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FriendValidator {

    private final FriendRepository friendRepository;
    private final UserQueryService userQueryService;

    public void ensureNotAlreadyFriend(Long userId, Long friendId) {
        if (friendRepository.existsByUsersAndStatus(userId, friendId, FriendStatus.ACCEPTED)) {
            throw new FriendException(ErrorStatus.ALREADY_FRIEND);
        }
    }

    public void ensureNotAlreadyRequested(Long userId, Long friendId) {
        if (friendRepository.existsByUsersAndStatus(userId, friendId, FriendStatus.REQUESTED) || friendRepository.existsByUsersAndStatus(userId, friendId, FriendStatus.REQUESTED)) {
            throw new FriendException(ErrorStatus.ALREADY_REQUESTED);
        }
    }

    public void ensureNotSelfRequest(Long userId, Long friendId) {
        if (userId == friendId) throw new FriendException(ErrorStatus.SAME_USER);
    }

    public void isFriendRequester(Friend friend, Long userId) {
        if (friend.getUser().getId().equals(userId)) throw new FriendException(ErrorStatus.SAME_USER);
    }

    public void ensureFriendUser(Long userId) {
        User user = userQueryService.getUserByUserId(userId);
        if (user.getUserActivate() != UserActivate.ACTIVE) throw new UserException(ErrorStatus.CAN_NOT_FRIEND_STATUS);
    }
}
