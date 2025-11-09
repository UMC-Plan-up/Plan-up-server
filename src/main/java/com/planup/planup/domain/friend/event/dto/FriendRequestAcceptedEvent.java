package com.planup.planup.domain.friend.event.dto;

public record FriendRequestAcceptedEvent(
        Long accepterId,     // 수락한 사람
        Long requesterId    // 신청 보낸 사람
) {
    public static FriendRequestAcceptedEvent of(Long accepterId, Long requesterId) {
        return new FriendRequestAcceptedEvent(accepterId, requesterId);
    }
}