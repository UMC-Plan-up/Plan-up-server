package com.planup.planup.domain.friend.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
@Slf4j
public class FriendWriteServiceImpl implements FriendWriteService {

    private final FriendRepository friendRepository;
    private final UserQueryService userService;
    private final FriendValidator friendValidator;
    private final UserBlockValidator userBlockValidator;
    private final ApplicationEventPublisher publisher;

    @Override
    public boolean deleteFriend(User user, Long friendId) {
        // 1. userId와 friendId로 Friend 엔티티를 찾는다.
        // 2. 해당 Friend 엔티티를 삭제한다.
        // 3. 성공적으로 삭제했으면 true, 아니면 false 반환

        Optional<Friend> optinalFriend = friendRepository.findByUserIdAndFriendIdAndStatus(
                FriendStatus.ACCEPTED, user.getId(), friendId);

        if (optinalFriend.isPresent()) {
            friendRepository.delete(optinalFriend.get());
            return true;
        }
        // 친구를 찾지 못했을 때
        throw new UserException(ErrorStatus._BAD_REQUEST);
    }

    @Override
    public boolean rejectFriendRequest(Long userId, Long friendId) {

        //나(userId)에게 친구 신청한 친구(friendId)를 찾음
        Optional<Friend> optionalFriend = friendRepository.findByUserIdAndFriendIdAndStatus(FriendStatus.REQUESTED, userId, friendId);

        if (optionalFriend.isPresent()) {
            optionalFriend.get().setStatus(FriendStatus.REJECTED);

            //알림 생성
            publisher.publishEvent(
                    FriendRejectSentEvent.of(friendId, userId)
            );

            return true;
        }

        throw new UserException(ErrorStatus._BAD_REQUEST);
    }

    @Override
    public boolean acceptFriendRequest(Long userId, Long friendId) {
        //나(userId)에게 친구 신청한 친구(friendId)를 찾음
        Optional<Friend> optionalFriend = friendRepository.findByUserIdAndFriendIdAndStatus(FriendStatus.REQUESTED, userId, friendId);

        if (optionalFriend.isPresent()) {
            Friend friend = optionalFriend.get();
            friend.setStatus(FriendStatus.ACCEPTED);

            //커밋 이후 알림 생성
            publisher.publishEvent(
                    FriendRequestAcceptedEvent.of(userId, friendId)
            );

            return true;
        }

        throw new UserException(ErrorStatus._BAD_REQUEST);
    }

    @Override
    public boolean createFriend(Long userId, Long friendId) {
        //에러 체크
        checkRequestSendFriend(userId, friendId);

        User user = userService.getUserByUserId(userId);
        User friend = userService.getUserByUserId(friendId);

        // Friend 엔티티 생성
        Friend friendRequest = Friend.builder()
                .user(user)
                .friend(friend)
                .status(FriendStatus.ACCEPTED)
                .build();

        friendRepository.save(friendRequest);
    }

    @Override
    public boolean sendFriendRequest(Long userId, Long friendId) {

        checkRequestSendFriend(userId, friendId);

        // 유저 엔티티 조회
        User user = userService.getUserByUserId(userId);
        User friendUser = userService.getUserByUserId(friendId);

        // Friend 엔티티 생성
        Friend friendRequest = Friend.builder()
                .user(user)
                .friend(friendUser)
                .status(FriendStatus.REQUESTED)
                .build();

        //TODO: 실제 서비스에서 제거
        if (user.getEmail().equals("dummy11@planup.com")) friendRequest.setStatus(FriendStatus.ACCEPTED);

        friendRepository.save(friendRequest);

        publisher.publishEvent(
                FriendRequestSentEvent.of(friendId, userId)
        );

        return true;
    }

    private void checkRequestSendFriend(Long userId, Long friendId) {
        //이미 친구 관계인지 확인
        friendValidator.ensureNotAlreadyFriend(userId, friendId);

        //자기 자신에게 보내는 에러인지 확인
        friendValidator.ensureNotSelfRequest(userId, friendId);

        //이미 신청했는지 확인
        friendValidator.ensureNotAlreadyRequested(userId, friendId);

        //차단된 사용자인지 확인
        userBlockValidator.ensureExistUserBlock(userId, friendId);
    }

}
