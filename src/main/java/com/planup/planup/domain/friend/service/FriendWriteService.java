package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.user.entity.User;

public interface FriendWriteService {
    boolean deleteFriend(User user, Long friendId);

    boolean blockFriend(User user, Long friendId);
}
