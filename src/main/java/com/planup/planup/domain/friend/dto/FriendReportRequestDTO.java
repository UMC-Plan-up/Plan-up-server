package com.planup.planup.domain.friend.dto;

import lombok.Getter;

@Getter
public class FriendReportRequestDTO {

    private Long friendId;
    private String reason;    // 신고 사유
    private boolean block;    // 차단 여부
}
