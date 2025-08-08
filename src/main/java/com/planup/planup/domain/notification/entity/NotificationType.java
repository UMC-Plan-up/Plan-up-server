package com.planup.planup.domain.notification.entity;

public enum NotificationType {

    /**
     * 무슨 종류의 알림인가에 대한 필드
     *
     * 예를들어, 나를 팔로우한 경우, 내 글에 댓글을 단 경우, 팀원이 목표를 달성한 경
     */

//    // 요청
//    CHALLENGE_REQUEST_SENT,
//    CHALLENGE_REQUEST_RECEIVED,
//
//    // 수락/거절
//    CHALLENGE_REQUEST_ACCEPTED,
//    CHALLENGE_REQUEST_REJECTED,
//
//    // 패널티 제안
//    PENALTY_PROPOSAL_SENT,
//    PENALTY_PROPOSAL_RECEIVED,
//
//    // 패널티 재제안
//    PENALTY_PROPOSAL_SENT_RE,
//    PENALTY_PROPOSAL_RECEIVED_RE,
//
//    // 패널티 수락/거절
//    PENALTY_ACCEPTED,
//    PENALTY_REJECTED,
//
//    // 패널티 거절로 챌린지 취소
//    PENALTY_REJECTED_CHALLENGE_CANCELLED,

    //친구의 목표 달성
    // 랭킹 변동 알림
    RANK_DOWN,  // 내 랭킹 하락

    // 목표 알림
    GOAL_REMINDER,  // 설정한 목표 알림 시각 알림

    // 친구 활동 알림
    FRIEND_GOAL_COMPLETED,  // 친구가 목표를 완료했을 때
    FRIEND_GOAL_CREATED,    // 친구가 새 목표를 추가했을 때
    GOAL_PART_UPDATED,       // 내가 참여한 목표의 세부항목이 수정된 경우

    // 댓글
    COMMENT_ON_VERIFICATION,  // 내 달성 확인에 댓글

    // 목표 피드백
    GOAL_CHEERED,    // 응원해요
    GOAL_ENCOURAGED, // 분발해요

    // 친구 요청/응답
    FRIEND_REQUEST_SENT,    // 친구 신청 보냄
    FRIEND_REQUEST_ACCEPTED, // 친구 수락됨
    FRIEND_REQUEST_REJECTED,  // 친구 신청 거절됨

    CHALLENGE_REQUEST_SENT,
    CHALLENGE_REQUEST_RECEIVED,
    CHALLENGE_REQUEST_ACCEPTED,
    CHALLENGE_REQUEST_REJECTED,
    PENALTY_PROPOSAL_SENT,
    PENALTY_PROPOSAL_RECEIVED,
    PENALTY_ACCEPTED,
    PENALTY_REJECTED,
    CHALLENGE_STARTED,
    CHALLENGE_ENDED,
    PENALTY_REMINDER_SENT
}
