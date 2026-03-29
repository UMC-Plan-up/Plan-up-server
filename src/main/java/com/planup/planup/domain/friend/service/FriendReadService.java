package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.entity.User;

import java.util.List;

public interface FriendReadService {
    List<User> getMyFriend(Long userId);

    //친구 리스트를 반환한다.
    FriendResponseDTO.FriendSummaryList getFriendSummaryList(Long userId);

    List<FriendResponseDTO.FriendInfoSummary> getRequestedFriends(Long userId);

    List<FriendResponseDTO.FriendInfoInChallengeCreate> getFriendListInChallenge(Long userId);

    void ensureFriendRelation(Long userId, Long friendId);

    boolean isFriendBoolean(Long userId, Long friendId);

    Integer getTodayTotalSecTimeByUserGoal(UserGoal userGoal);
}
