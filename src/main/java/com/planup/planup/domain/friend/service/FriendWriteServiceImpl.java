package com.planup.planup.domain.friend.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.friend.dto.FriendReportRequestDTO;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.user.entity.User;
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

    @Override
    public boolean deleteFriend(User user, Long friendId) {
        // 1. userId와 friendId로 Friend 엔티티를 찾는다.
        // 2. 해당 Friend 엔티티를 삭제한다.
        // 3. 성공적으로 삭제했으면 true, 아니면 false 반환

        Optional<Friend> optinalFriend = friendRepository.findByUserIdAndFriendId(
                FriendStatus.ACCEPTED, user.getId(), friendId);

        if (optinalFriend.isPresent()) {
            friendRepository.delete(optinalFriend.get());
            return true;
        }
        // 친구를 찾지 못했을 때
        throw new UserException(ErrorStatus._BAD_REQUEST);
    }

    @Override
    public boolean blockFriend(User user, Long friendId) {

        Optional<Friend> optionalFriend = friendRepository.findByUserIdAndFriendId(
                FriendStatus.ACCEPTED, user.getId(), friendId);

        if (optionalFriend.isPresent()) {
            optionalFriend.get().setStatus(FriendStatus.BLOCKED);
            return true;
        }

        throw new UserException(ErrorStatus._BAD_REQUEST);
    }

    //TODO: 지금 reason이 전혀 사용되지 않고 있는데 정상적으로 만들어진 메서드인지 확인 필요
    @Override
    public boolean reportFriend(FriendReportRequestDTO request) {

        //request에서 값 꺼내기
        Long userId = request.getUserId();
        Long friendId= request.getFriendId();
        String reason = request.getReason();
        boolean block = request.isBlock();

        Optional<Friend> targetFriend = friendRepository.findByUserIdAndFriendIdAndStatusNot(userId, friendId, FriendStatus.REQUESTED);

        if (targetFriend.isPresent()) {
            targetFriend.get().setStatus(FriendStatus.BLOCKED);
            return true;
        }
        // 친구를 찾지 못했을 때
        throw new UserException(ErrorStatus._BAD_REQUEST);
    }
}
