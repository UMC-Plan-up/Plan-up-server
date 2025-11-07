package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.friend.converter.FriendConverter;
import com.planup.planup.domain.friend.dto.BlockedFriendResponseDTO;
import com.planup.planup.domain.friend.entity.UserBlock;
import com.planup.planup.domain.friend.repository.UserBlockRepository;
import com.planup.planup.domain.friend.service.policy.UserBlockValidator;
import com.planup.planup.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserBlockServiceImpl {

    private final UserBlockRepository userBlockRepository;
    private final FriendConverter friendConverter;
    private final UserBlockValidator userBlockValidator;
    private final UserRead\\\

    public List<BlockedFriendResponseDTO> getBlockedFriends(Long userId) {
        List<UserBlock> friends = userBlockRepository.findBlockedByBlockerId(userId);
        List<User> blockedUsers = friends.stream().map(UserBlock::getBlocked).collect(Collectors.toList());

        return friendConverter.toBlockedFriendDTO(userId, blockedUsers);
    }

    public UserBlock getBlockedFriend(Long userId, Long friendId) {
        Optional<UserBlock> optionalUserBlock = userBlockRepository.findByUserIdAndFriendId(userId, friendId);
        userBlockValidator.ensureExistUserBlock(optionalUserBlock);

        return optionalUserBlock.get();
    }

    @Transactional
    public Long unblockFriend(Long userId, Long friendId) {
        Optional<UserBlock> optionalUserBlock = userBlockRepository.findByUserIdAndFriendIdWithBlocked(userId, friendId);

        userBlockValidator.ensureExistUserBlock(optionalUserBlock);

        userBlockRepository.deleteById(optionalUserBlock.get().getId());

        return optionalUserBlock.get().getId();
    }

    @Transactional
    public boolean blockFriend(User user, Long friendId) {

        user
    }
}
