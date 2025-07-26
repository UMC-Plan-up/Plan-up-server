package com.planup.planup.domain.notification.entity;

public enum TargetType {
    /**
     * 알림이 가르키는 대상
     *
     * 친구가 내 글에 댓글을 달아서 발생했다면 해당 글
     * 나를 팔로우 한 경우, 팔로우 한 사람
     * 팀원이 목표를 달성한 경우, 목표
     */

    GOAL, USER, REPORT,

    CHALLENGE, PENALTY
}
