package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.friend.dto.FriendResponseDTO;

import java.util.List;

public interface FriendReadService {
    List<FriendResponseDTO.FriendInfoSummary> getRequestedFriends(Long userId);
}
