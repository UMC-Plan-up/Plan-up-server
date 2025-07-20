package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.friend.dto.FriendResponseDTO;

import java.util.List;

public interface FriendService {
    List<FriendResponseDTO.FriendSummaryList> getFriendSummeryList(Long userId);

    // 친구 삭제
    boolean deleteFriend(Long userId, Long friendId);
}
