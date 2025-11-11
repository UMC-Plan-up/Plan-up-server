package com.planup.planup.domain.friend.service.userBlockService;

import com.planup.planup.domain.friend.converter.FriendConverter;
import com.planup.planup.domain.friend.dto.BlockedFriendResponseDTO;
import com.planup.planup.domain.friend.dto.UnblockFriendRequestDTO;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.entity.UserBlock;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.friend.repository.UserBlockRepository;
import com.planup.planup.domain.friend.service.FriendReadService;
import com.planup.planup.domain.friend.service.policy.UserBlockValidator;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
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
public class UserBlockServiceImpl implements UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final FriendConverter friendConverter;
    private final UserBlockValidator userBlockValidator;
    private final UserService userService;
    private final FriendRepository friendRepository;

    @Override
    public List<BlockedFriendResponseDTO> getBlockedFriends(Long userId) {
        List<UserBlock> friends = userBlockRepository.findBlockedByBlockerId(userId);
        List<User> blockedUsers = friends.stream().map(UserBlock::getBlocked).collect(Collectors.toList());

        return friendConverter.toBlockedFriendDTO(userId, blockedUsers);
    }
    @Override
    public UserBlock getBlockedFriend(Long userId, Long friendId) {
        Optional<UserBlock> optionalUserBlock = userBlockRepository.findByBlockerIdAndBlockedId(userId, friendId);
        userBlockValidator.ensureExistUserBlock(optionalUserBlock);

        return optionalUserBlock.get();
    }

    @Override
    @Transactional
    public Long unblockFriend(UnblockFriendRequestDTO request) {
        Long userId = request.getUserId();
        Long friendId = request.getFriendId();

        Optional<UserBlock> optionalUserBlock = userBlockRepository.findByUserIdAndFriendIdWithBlocked(userId, friendId);

        userBlockValidator.ensureExistUserBlock(optionalUserBlock);

        userBlockRepository.deleteById(optionalUserBlock.get().getId());

        return optionalUserBlock.get().getId();
    }

    @Transactional
    @Override
    public boolean blockFriend(User user, Long friendId) {

        //차단당하는 상대방 조회
        User blocked = userService.getUserbyUserId(friendId);

        //이미 존재하는 차단인지 확인
        userBlockValidator.ensureExistUserBlock(user.getId(), friendId);

        //생성 및 저장
        UserBlock userBlock = UserBlock.builder()
                .blocker(user)
                .blocked(blocked)
                .build();

        userBlockRepository.save(userBlock);

        //친구 관계라면 자동 취소
        friendRepository.findByUserIdAndFriendIdAndStatus(FriendStatus.ACCEPTED, friendId, user.getId());

        return true;
    }
}
