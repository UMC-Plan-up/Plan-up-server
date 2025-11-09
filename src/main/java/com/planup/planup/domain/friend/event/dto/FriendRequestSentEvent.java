package com.planup.planup.domain.friend.event.dto;

public record FriendRequestSentEvent(
        Long receiverId,     // 수락한 사람
        Long senderId    // 신청 보낸 사람
) {
    public static FriendRequestAcceptedEvent of(Long receiverId, Long senderId) {
        return new FriendRequestAcceptedEvent(receiverId, senderId);
    }
}
