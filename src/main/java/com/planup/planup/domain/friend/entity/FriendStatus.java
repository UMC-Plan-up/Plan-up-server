package com.planup.planup.domain.friend.entity;

public enum FriendStatus {
    REQUESTED, REJECTED, ACCEPTED, UNFRIENDED, CANCELED;

    //UNFRIENDED: 친구 신청을 수락했다가 취소된 경우
    //CANCELED: 친구 요청을 보냈지만 수락 전에 취소한 경우
}
