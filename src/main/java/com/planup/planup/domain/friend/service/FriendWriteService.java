package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.friend.dto.FriendReportRequestDTO;
import com.planup.planup.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

public interface FriendWriteService {
    boolean deleteFriend(User user, Long friendId);

    boolean rejectFriendRequest(Long userId, Long friendId);

    boolean acceptFriendRequest(Long userId, Long friendId);

    boolean sendFriendRequest(Long userId, Long friendId);
}
