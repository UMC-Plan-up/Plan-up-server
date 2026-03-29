package com.planup.planup.domain.friend.service.userBlockService;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.FriendException;
import com.planup.planup.domain.friend.converter.FriendConverter;
import com.planup.planup.domain.friend.dto.BlockedFriendResponseDTO;
import com.planup.planup.domain.friend.dto.UnblockFriendRequestDTO;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.entity.UserBlock;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.friend.repository.UserBlockRepository;
import com.planup.planup.domain.friend.service.policy.UserBlockValidator;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.query.UserQueryService;
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
    private final UserQueryService userService;
    private final FriendRepository friendRepository;

    @Override
    public List<BlockedFriendResponseDTO> getBlockedFriends(Long userId) {
        List<UserBlock> blockedRelations = userBlockRepository.findBlockedByBlockerId(userId);
        List<User> blockedUsers = blockedRelations.stream().map(UserBlock::getBlocked).toList();

        return friendConverter.toBlockedFriendDTO(userId, blockedUsers);
    }
    @Override
    public UserBlock getBlockedFriend(Long userId, Long friendId) {
        return userBlockRepository.findByBlockerIdAndBlockedId(userId, friendId)
                .orElseThrow(() -> new FriendException(ErrorStatus.NOT_EXIST_USERBLOCK));
    }

    @Override
    @Transactional
    public Long unblockFriend(Long userId, UnblockFriendRequestDTO request) {
        Long friendId = request.getFriendId();

        UserBlock blockedFriend = getBlockedFriend(userId, friendId);
        userBlockRepository.deleteById(blockedFriend.getId());

        return blockedFriend.getId();
    }

    @Transactional
    @Override
    public boolean blockFriend(User user, Long friendId) {

        //차단당하는 상대방 조회
        User blocked = userService.getUserByUserId(friendId);

        //이미 존재하는 차단인지 확인
        userBlockValidator.ensureExistUserBlock(user.getId(), friendId);

        //생성 및 저장
        UserBlock userBlock = UserBlock.builder()
                .blocker(user)
                .blocked(blocked)
                .build();

        userBlockRepository.save(userBlock);

        //친구 관계라면 자동 취소
        cleanupFriendRelationOnBlock(user, friendId);
        log.info("Block user. blockerId={}, blockedId={}", user.getId(), friendId);
        return true;
    }

    private void cleanupFriendRelationOnBlock(User blocker, Long blockedId) {
        Optional<Friend> acceptedRelation = friendRepository.findByUserIdAndFriendIdAndStatus(FriendStatus.ACCEPTED, blockedId, blocker.getId());

        acceptedRelation.ifPresent(friend -> {
            friend.setStatus(FriendStatus.UNFRIENDED);
            log.info("Unfriend due to block. blockerId={}, blockedId={}", blocker.getId(), blockedId);
        });

        Optional<Friend> pendingRequest = friendRepository.findByUserIdAndFriendIdAndStatus(FriendStatus.REQUESTED, blockedId, blocker.getId());

        pendingRequest.ifPresent(friend -> {
            friend.setStatus(FriendStatus.REJECTED);
            log.info("Reject pending friend request due to block. blockerId={}, blockedId={}", blocker.getId(), blockedId);
        });
    }
}
