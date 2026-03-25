package com.planup.planup.domain.friend.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.FriendException;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.event.dto.FriendRejectSentEvent;
import com.planup.planup.domain.friend.event.dto.FriendRequestAcceptedEvent;
import com.planup.planup.domain.friend.event.dto.FriendRequestSentEvent;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.friend.service.policy.FriendValidator;
import com.planup.planup.domain.friend.service.policy.UserBlockValidator;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.query.UserQueryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
@Slf4j
public class FriendWriteServiceImpl implements FriendWriteService {

    private final FriendRepository friendRepository;
    private final UserQueryService userQueryService ;
    private final FriendValidator friendValidator;
    private final UserBlockValidator userBlockValidator;
    private final ApplicationEventPublisher publisher;

    @Override
    public void deleteFriend(User user, Long friendId) {
        // 1. userId와 friendId로 Friend 엔티티를 찾는다.
        // 2. 해당 Friend 엔티티를 삭제한다.
        // 3. 성공적으로 삭제했으면 true, 아니면 false 반환

        Friend friend = findByUserIdAndFriendId(FriendStatus.ACCEPTED, user.getId(), friendId, ErrorStatus.NOT_FRIEND);

        friendRepository.delete(friend);
        log.info("Delete friend relation. userId={}, friendId={}", user.getId(), friendId);
    }

    @Override
    public void rejectFriendRequest(Long userId, Long friendId) {

        //나(userId)에게 친구 신청한 친구(friendId)를 찾음
        Friend friend = findByUserIdAndFriendId(FriendStatus.REQUESTED, userId, friendId, ErrorStatus.NO_REJECTABLE_FRIEND_REQUEST);

        friend.setStatus(FriendStatus.REJECTED);

        //알림 생성
        publisher.publishEvent(
                FriendRejectSentEvent.of(friendId, userId)
        );
        log.info("Reject friend request. receiverId={}, senderId={}", userId, friendId);
    }

    @Override
    public boolean acceptFriendRequest(Long userId, Long friendId) {
        //나(userId)에게 친구 신청한 친구(friendId)를 찾음
        Friend friend = findByUserIdAndFriendId(FriendStatus.REQUESTED, userId, friendId, ErrorStatus.NO_ACCEPTABLE_FRIEND_REQUEST);

        //셀프 수락 여부 확인
        validateAcceptFriendRequest(userId, friendId, friend);
        friend.setStatus(FriendStatus.ACCEPTED);

        //커밋 이후 알림 생성
        publisher.publishEvent(
                FriendRequestAcceptedEvent.of(userId, friendId)
        );
        log.info("Accept friend request. receiverId={}, senderId={}", userId, friendId);

        return true;
    }

    @Override
    public boolean createFriend(User user, User friend) {
        //에러 체크
        checkRequestSendFriend(user.getId(), friend.getId());

        // Friend 엔티티 생성
        Friend friendRequest = Friend.builder()
                .user(user)
                .friend(friend)
                .status(FriendStatus.ACCEPTED)
                .build();

        friendRepository.save(friendRequest);
        return true;
    }

    @Override
    public boolean sendFriendRequest(Long userId, Long friendId) {

        checkRequestSendFriend(userId, friendId);

        // 유저 엔티티 조회
        User user = userQueryService.getUserByUserId(userId);
        User friendUser = userQueryService.getUserByUserId(friendId);

        // Friend 엔티티 생성
        Friend friendRequest = Friend.builder()
                .user(user)
                .friend(friendUser)
                .status(FriendStatus.REQUESTED)
                .build();

        //TODO: 실제 서비스에서 제거
        if (user.getEmail().equals("dummy11@planup.com")) friendRequest.setStatus(FriendStatus.ACCEPTED);

        try {
            friendRepository.save(friendRequest);
        } catch (DataIntegrityViolationException e) {                               //데이터베이스 제약을 위반한 경우 발생하는 에러
            log.warn("Duplicate friend request detected. userId={}, friendId={}", userId, friendId);
            throw new UserException(ErrorStatus.ALREADY_REQUESTED_FRIEND);
        }

        publisher.publishEvent(
                FriendRequestSentEvent.of(friendId, userId)
        );
        log.info("Send friend request. receiverId={}, senderId={}", userId, friendId);

        return true;
    }

    private void checkRequestSendFriend(Long userId, Long friendId) {
        //이미 친구 관계인지 확인
        friendValidator.ensureNotAlreadyFriend(userId, friendId);

        //지금 친구 신청이 가능한 친구인지 확인
        friendValidator.ensureFriendUser(userId);

        //자기 자신에게 보내는 에러인지 확인
        friendValidator.ensureNotSelfRequest(userId, friendId);

        //이미 신청했는지 확인
        friendValidator.ensureNotAlreadyRequested(userId, friendId);

        //차단된 사용자인지 확인
        userBlockValidator.ensureExistUserBlock(userId, friendId);
    }

    private void validateAcceptFriendRequest(Long userId, Long friendId, Friend friend) {
        friendValidator.ensureFriendRequester(friend, userId);
        userBlockValidator.ensureExistUserBlock(userId, friendId);
        friendValidator.ensureFriendUser(userId);
    }

    private Friend findByUserIdAndFriendId(FriendStatus requested, Long userId, Long friendId, ErrorStatus noRejectableFriendRequest) {
        return friendRepository.findByUserIdAndFriendIdAndStatus(requested, userId, friendId)
                .orElseThrow(() -> new FriendException(noRejectableFriendRequest));
    }
}
