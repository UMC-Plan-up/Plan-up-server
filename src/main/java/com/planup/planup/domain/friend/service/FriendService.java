package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.friend.dto.FriendResponseDTO;

import java.util.List;

public interface FriendService {
    List<FriendResponseDTO.FriendSummaryList> getFriendSummeryList(Long userId);

    // 친구 삭제
    boolean deleteFriend(Long userId, Long friendId);

    // 친구 차단
    boolean blockFriend(Long userId, Long friendId);

    // 친구 신고
    boolean reportFriend(Long userId, Long friendId, String reason, boolean block);
}
