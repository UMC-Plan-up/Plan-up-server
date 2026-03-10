package com.planup.planup.domain.friend.dto;

import com.planup.planup.domain.user.enums.SanctionDetailReason;
import lombok.Getter;

@Getter
public class FriendReportRequestDTO {
    private Long friendId;
    private SanctionDetailReason reason;    // 신고 사유
    private boolean block;                  // 차단 여부
}
