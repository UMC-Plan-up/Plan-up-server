package com.planup.planup.domain.friend.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
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

        Optional<Friend> optinalFriend = friendRepository.findAcceptedByUserId(
                FriendStatus.ACCEPTED, user.getId(), friendId);

        if (optinalFriend.isPresent()) {
            friendRepository.delete(optinalFriend.);
            return true;
        }
        // 친구를 찾지 못했을 때
        throw new UserException(ErrorStatus._BAD_REQUEST);
    }
}
