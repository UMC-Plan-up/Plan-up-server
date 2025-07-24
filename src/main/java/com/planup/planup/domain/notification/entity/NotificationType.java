package com.planup.planup.domain.notification.entity;

public enum NotificationType {

    /**
     * 무슨 종류의 알림인가에 대한 필드
     *
     * 예를들어, 나를 팔로우한경우, 내 글에 댓글을 단 경우, 팀원이 목표를 달성한 경
     */

    // 요청
    CHALLENGE_REQUEST_SENT,
    CHALLENGE_REQUEST_RECEIVED,

    // 수락/거절
    CHALLENGE_REQUEST_ACCEPTED,
    CHALLENGE_REQUEST_REJECTED,

    // 패널티 제안
    PENALTY_PROPOSAL_SENT,
    PENALTY_PROPOSAL_RECEIVED,

    // 패널티 제안
    PENALTY_PROPOSAL_SENT_RE,
    PENALTY_PROPOSAL_RECEIVED_RE,

    // 패널티 수락/거절
    PENALTY_ACCEPTED,
    PENALTY_REJECTED,

    // 패널티 거절로 챌린지 취소
    PENALTY_REJECTED_CHALLENGE_CANCELLED
}
