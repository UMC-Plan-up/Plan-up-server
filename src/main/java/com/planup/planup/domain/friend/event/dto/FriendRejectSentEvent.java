package com.planup.planup.domain.friend.event.dto;

public record FriendRejectSentEvent(
        Long receiverId,
        Long senderId
) {
    public static FriendRejectSentEvent of(Long accepterId, Long senderId) {
        return new FriendRejectSentEvent(
                accepterId,
                senderId
        );
    }
}
