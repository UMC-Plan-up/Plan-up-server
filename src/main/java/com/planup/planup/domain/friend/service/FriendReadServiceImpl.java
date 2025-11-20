package com.planup.planup.domain.friend.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.FriendException;
import com.planup.planup.domain.friend.converter.FriendConverter;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.friend.entity.Friend;
import com.planup.planup.domain.friend.entity.FriendStatus;
import com.planup.planup.domain.friend.repository.FriendRepository;
import com.planup.planup.domain.friend.service.policy.FriendSelector;
import com.planup.planup.domain.friend.service.policy.FriendSummaryAssembler;
import com.planup.planup.domain.goal.dto.UserWithGoalCountDTO;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import com.planup.planup.domain.verification.service.TimerVerificationReadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.planup.planup.domain.friend.entity.FriendStatus.ACCEPTED;

@Transactional(readOnly = true)
@Service
@AllArgsConstructor
@Slf4j
public class FriendReadServiceImpl implements FriendReadService {

    private final FriendRepository friendRepository;
    private final UserService userService;
    private final FriendConverter friendConverter;
    private final UserGoalRepository userGoalRepository;
    private final FriendSummaryAssembler friendSummaryAssembler;

    //친구 리스트를 반환한다.
    @Override
    public FriendResponseDTO.FriendSummaryList getFriendSummeryList(Long userId) {

        User me = userService.getUserbyUserId(userId);

        List<Friend> relations = friendRepository.findListByUserIdWithUsers(ACCEPTED, me.getId());
        List<User> friends = relations.stream().map(Friend::getFriend).toList();

        return FriendConverter.toFriendSummaryList(
                friends.stream()
                        .map(friendSummaryAssembler::assemble)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public List<FriendResponseDTO.FriendInfoSummary> getRequestedFriends(Long userId) {
        List<Friend> friendRequests = friendRepository.findByStatusAndFriendIdOrderByCreatedAtDescWithUser(FriendStatus.REQUESTED, userId);

        //데이터가 없으면 빈 리스트를 만환한다.
        if (friendRequests.isEmpty()) return Collections.emptyList();

        return friendRequests.stream()
                .map(Friend::getUser)
                .map(friendSummaryAssembler::assemble)
                .collect(Collectors.toList());
    }

    @Override
    public List<FriendResponseDTO.FriendInfoInChallengeCreate> getFriendListInChallenge(Long userId) {
        //친구를 조회하여 친구 매핑을 리스트로 반환
        List<Friend> friendList = friendRepository.findListByUserIdWithUsers(ACCEPTED, userId);

        //친구 매핑에서 친구 아이디를 추출
        List<Long> friendIds = friendList.stream().map(friend -> friend.getFriendNotMe(userId).getId()).toList();

        List<UserWithGoalCountDTO> userGoalCntByUserIds = userGoalRepository.getUserGoalCntByUserIds(friendIds);
        return friendConverter.toFriendInfoChallenge(userGoalCntByUserIds);
    }

    @Override
    public void isFriend(Long userId, Long friendId) {
        Optional<Friend> optionalFriend = friendRepository.findByUserIdAndFriendIdAndStatus(ACCEPTED, userId, friendId);
        if (optionalFriend.isEmpty()) {
            throw new FriendException(ErrorStatus.NOT_EXIST_USERBLOCK);
        }
    }
}
