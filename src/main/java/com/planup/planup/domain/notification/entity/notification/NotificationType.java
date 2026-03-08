package com.planup.planup.domain.notification.entity.notification;

public enum NotificationType {

    /**
     * 무슨 종류의 알림인가에 대한 필드
     *
     * 예를들어, 나를 팔로우한 경우, 내 글에 댓글을 단 경우, 팀원이 목표를 달성한 경
     */

    RANK_DOWN(NotificationGroup.SERVICE),
    GOAL_REMINDER(NotificationGroup.SERVICE),

    FRIEND_GOAL_COMPLETED(NotificationGroup.SERVICE),
    FRIEND_GOAL_CREATED(NotificationGroup.SERVICE),
    GOAL_PART_UPDATED(NotificationGroup.SERVICE),

    COMMENT_ON_VERIFICATION(NotificationGroup.SERVICE),
    FEEDBACK_CHEERED(NotificationGroup.SERVICE),
    FEEDBACK_ENCOURAGED(NotificationGroup.SERVICE),

    FRIEND_REQUEST_SENT(NotificationGroup.SERVICE),
    FRIEND_REQUEST_ACCEPTED(NotificationGroup.SERVICE),
    FRIEND_REQUEST_REJECTED(NotificationGroup.SERVICE),

    CHALLENGE_REQUEST_SENT(NotificationGroup.SERVICE),
    CHALLENGE_REQUEST_RECEIVED(NotificationGroup.SERVICE),
    CHALLENGE_REQUEST_ACCEPTED(NotificationGroup.SERVICE),
    CHALLENGE_REQUEST_REJECTED(NotificationGroup.SERVICE),
    PENALTY_PROPOSAL_SENT(NotificationGroup.SERVICE),
    PENALTY_PROPOSAL_RECEIVED(NotificationGroup.SERVICE),
    PENALTY_ACCEPTED(NotificationGroup.SERVICE),
    PENALTY_REJECTED(NotificationGroup.SERVICE),
    CHALLENGE_STARTED(NotificationGroup.SERVICE),
    CHALLENGE_ENDED(NotificationGroup.SERVICE),
    PENALTY_REMINDER_SENT(NotificationGroup.SERVICE);

    private final NotificationGroup group;

    NotificationType(NotificationGroup group) {
        this.group = group;
    }

    public NotificationGroup getGroup() {
        return group;
    }

}
