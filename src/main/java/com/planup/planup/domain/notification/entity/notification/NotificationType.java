package com.planup.planup.domain.notification.entity.notification;

public enum NotificationType {

    /**
     * 무슨 종류의 알림인가에 대한 필드
     *
     * 예를들어, 나를 팔로우한 경우, 내 글에 댓글을 단 경우, 팀원이 목표를 달성한 경
     */

    RANK_DOWN(NotificationGroup.GOAL),
    GOAL_REMINDER(NotificationGroup.GOAL),

    FRIEND_GOAL_COMPLETED(NotificationGroup.GOAL),
    FRIEND_GOAL_CREATED(NotificationGroup.GOAL),
    GOAL_PART_UPDATED(NotificationGroup.GOAL),

    COMMENT_ON_VERIFICATION(NotificationGroup.FEEDBACK),
    FEEDBACK_CHEERED(NotificationGroup.FEEDBACK),
    FEEDBACK_ENCOURAGED(NotificationGroup.FEEDBACK),

    FRIEND_REQUEST_SENT(NotificationGroup.GOAL),
    FRIEND_REQUEST_ACCEPTED(NotificationGroup.GOAL),
    FRIEND_REQUEST_REJECTED(NotificationGroup.GOAL),

    CHALLENGE_REQUEST_SENT(NotificationGroup.CHALLENGE),
    CHALLENGE_REQUEST_RECEIVED(NotificationGroup.CHALLENGE),
    CHALLENGE_REQUEST_ACCEPTED(NotificationGroup.CHALLENGE),
    CHALLENGE_REQUEST_REJECTED(NotificationGroup.CHALLENGE),
    PENALTY_PROPOSAL_SENT(NotificationGroup.CHALLENGE),
    PENALTY_PROPOSAL_RECEIVED(NotificationGroup.CHALLENGE),
    PENALTY_ACCEPTED(NotificationGroup.CHALLENGE),
    PENALTY_REJECTED(NotificationGroup.CHALLENGE),
    CHALLENGE_STARTED(NotificationGroup.CHALLENGE),
    CHALLENGE_ENDED(NotificationGroup.CHALLENGE),
    PENALTY_REMINDER_SENT(NotificationGroup.CHALLENGE);

    private final NotificationGroup group;

    NotificationType(NotificationGroup group) {
        this.group = group;
    }

    public NotificationGroup getGroup() {
        return group;
    }

    public enum NotificationGroup { GOAL, CHALLENGE, FEEDBACK, ETC }
}
