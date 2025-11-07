package com.planup.planup.domain.friend.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.FriendException;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.friend.dto.FriendReportRequestDTO;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.friend.service.policy.FriendValidator;
import com.planup.planup.domain.friend.service.policy.UserBlockValidator;
import com.planup.planup.domain.notification.entity.NotificationType;
import com.planup.planup.domain.notification.entity.TargetType;
import com.planup.planup.domain.notification.service.NotificationService;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
@AllArgsConstructor
@Slf4j
public class FriendWriteServiceImpl implements FriendWriteService {

    private final FriendRepository friendRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final FriendValidator friendValidator;
    private final UserBlockServiceImpl userBlockService;
    private final UserBlockValidator userBlockValidator;

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

            notificationService.createNotification(
                    friendId,           // receiverId (친구 신청을 보낸 사람)
                    userId,             // senderId (친구 신청을 거절한 사람)
                    NotificationType.FRIEND_REQUEST_REJECTED,
                    TargetType.USER,
                    userId              // targetId (친구 신청을 거절한 사람의 ID)
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

            // 친구 신청 수락 알림 생성 - 양쪽 모두에게
            // 1. 친구 신청을 보낸 사람에게 알림
            notificationService.createNotification(
                    friendId,           // receiverId (친구 신청을 보낸 사람)
                    userId,             // senderId (친구 신청을 수락한 사람)
                    NotificationType.FRIEND_REQUEST_ACCEPTED,
                    TargetType.USER,
                    userId              // targetId (친구 신청을 수락한 사람의 ID)
            );

            // 2. 친구 신청을 수락한 사람에게도 알림
            notificationService.createNotification(
                    userId,             // receiverId (친구 신청을 수락한 사람)
                    friendId,           // senderId (친구 신청을 보낸 사람)
                    NotificationType.FRIEND_REQUEST_ACCEPTED,
                    TargetType.USER,
                    friendId            // targetId (친구 신청을 보낸 사람의 ID)
            );

            return true;
        }

        throw new UserException(ErrorStatus._BAD_REQUEST);
    }

    @Override
    public boolean sendFriendRequest(Long userId, Long friendId) {

        checkRequestSendFriend(userId, friendId);

        // 유저 엔티티 조회
        User user = userService.getUserbyUserId(userId);
        User friendUser = userService.getUserbyUserId(friendId);

        // Friend 엔티티 생성
        Friend friendRequest = Friend.builder()
                .user(user)
                .friend(friendUser)
                .status(FriendStatus.REQUESTED)
                .build();

        //TODO: 실제 서비스에서 제거
        if (user.getEmail().equals("dummy11@planup.com")) friendRequest.setStatus(FriendStatus.ACCEPTED);

        friendRepository.save(friendRequest);

        // 친구 신청 알림 생성
        notificationService.createNotification(
                friendId,           // receiverId (친구 신청 받는 사람)
                userId,             // senderId (친구 신청 보내는 사람)
                NotificationType.FRIEND_REQUEST_SENT,
                TargetType.USER,
                userId              // targetId (친구 신청 보낸 사람의 ID)
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
