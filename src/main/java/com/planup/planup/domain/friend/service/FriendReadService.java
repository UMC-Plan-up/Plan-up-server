package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.friend.dto.BlockedFriendResponseDTO;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;

import java.util.List;

public interface FriendReadService {
    List<FriendResponseDTO.FriendInfoSummary> getRequestedFriends(Long userId);

    List<FriendResponseDTO.FriendInfoInChallengeCreate> getFrinedListInChallenge(Long userId);

    List<BlockedFriendResponseDTO> getBlockedFriends(Long userId);
}
