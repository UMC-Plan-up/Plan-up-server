package com.planup.planup.domain.friend.service;

import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.friend.dto.BlockedFriendResponseDTO;
import com.planup.planup.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FriendService {
    List<FriendResponseDTO.FriendSummaryList> getFriendSummeryList(Long userId);

    // 친구 삭제
    boolean deleteFriend(User user, Long friendId);

    // 친구 차단
    boolean blockFriend(User user, Long friendId);

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

    // 차단된 친구 목록 조회
    List<BlockedFriendResponseDTO> getBlockedFriends(Long userId);

    // 친구 차단 해제 (닉네임으로)
    Long unblockFriend(Long userId, String friendNickname);

    List<FriendResponseDTO.FriendInfoInChallengeCreate> getFrinedListInChallenge(Long userId);

    @Transactional(readOnly = true)
    void isFriend(Long userId, Long creatorId);
}
