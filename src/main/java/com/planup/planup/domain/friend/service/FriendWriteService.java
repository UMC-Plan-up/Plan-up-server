package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.friend.dto.FriendReportRequestDTO;
import com.planup.planup.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

public interface FriendWriteService {
    boolean deleteFriend(User user, Long friendId);

    boolean blockFriend(User user, Long friendId);

    @Transactional
    boolean reportFriend(FriendReportRequestDTO request);

    boolean rejectFriendRequest(Long userId, Long friendId);
}
