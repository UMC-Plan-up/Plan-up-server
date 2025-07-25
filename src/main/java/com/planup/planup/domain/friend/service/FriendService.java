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

    // 받은 친구 신청 목록 조회
    List<FriendResponseDTO.FriendInfoSummary> getRequestedFriends(Long userId);

    // 친구 신청 거절
    boolean rejectFriendRequest(Long userId, Long friendId);

    // 친구 신청 수락
    boolean acceptFriendRequest(Long userId, Long friendId);

    // 친구 신청
    boolean sendFriendRequest(Long userId, Long friendId);
}
