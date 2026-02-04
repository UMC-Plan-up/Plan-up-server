package com.planup.planup.domain.notification.message;

import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.notification.entity.NotificationType;

public record MessageContext(
        NotificationType type,
        String senderName,
        String receiverName,
        Long targetId,            // goalId 등 (nullable)
        String updatedPartsStr,   // nullable
        String goalName           // nullable: 미리 알고 있으면 채워서 전달
) {

    public static MessageContext of(Notification n) {
        return new MessageContext(
                n.getType(),
                n.getSender().getNickname(),
                n.getReceiver().getNickname(),
                n.getTargetId(),
                n.getUpdatedGoalInfo(),
                null // goalName은 필요 시 내부에서 조회
        );
    }
    public boolean needsGoalName() {
        return switch (type) {
            case FEEDBACK_CHEERED, FEEDBACK_ENCOURAGED,
                    GOAL_REMINDER, FRIEND_GOAL_COMPLETED,
                    FRIEND_GOAL_CREATED, GOAL_PART_UPDATED -> true;
            default -> false;
        };
    }
}
