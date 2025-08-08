package com.planup.planup.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationMessageContext {
    private Integer rank;  // 랭킹 알림용
    private String senderName;
    private String receiverName;
    private String goalName;
    private String content;
}
