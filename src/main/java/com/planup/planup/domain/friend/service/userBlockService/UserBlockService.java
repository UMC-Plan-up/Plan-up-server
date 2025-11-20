package com.planup.planup.domain.friend.service.userBlockService;

import com.planup.planup.domain.friend.dto.BlockedFriendResponseDTO;
import com.planup.planup.domain.friend.dto.UnblockFriendRequestDTO;
import com.planup.planup.domain.friend.entity.UserBlock;
import com.planup.planup.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserBlockService {
    List<BlockedFriendResponseDTO> getBlockedFriends(Long userId);

    UserBlock getBlockedFriend(Long userId, Long friendId);

    Long unblockFriend(Long userId, UnblockFriendRequestDTO request);

    boolean blockFriend(User user, Long friendId);
}
