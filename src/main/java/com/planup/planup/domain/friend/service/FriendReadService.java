package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;

import java.util.List;

public interface FriendReadService {
    //친구 리스트를 반환한다.
    FriendResponseDTO.FriendSummaryList getFriendSummeryList(Long userId);

    List<FriendResponseDTO.FriendInfoSummary> getRequestedFriends(Long userId);

    List<FriendResponseDTO.FriendInfoInChallengeCreate> getFriendListInChallenge(Long userId);

    void isFriend(Long userId, Long friendId);

    Integer getTodayTotalSecTimeByUserGoal(UserGoal userGoal);
}
