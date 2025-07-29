package com.planup.planup.domain.bedge.service;

import com.planup.planup.domain.bedge.entity.UserStat;

public interface BadgeService {
    //가입 후 3일 이내 초대 코드 공유
    boolean checkInfluentialStarterBadge(UserStat userStat);

    //초대 코드 3회 이상 공유
    boolean checkWordOfMouthMasterBadge(UserStat userStat);

    //초대한 친구 3명 이상 가입
    boolean checkMagnetUserBadge(UserStat userStat);

    //하루에 친구 신청 3회 이상
    boolean checkFriendlyMaxBadge(UserStat userStat);

    //첫 댓글 남기기
    boolean checkFirstCommentBadge(UserStat userStat);

    //하루에 친구 신청 3회 이상
    boolean checkFriendRequestKingBadge(UserStat userStat);

    //친구 프로필 클릭 5회 이상
    boolean checkProfileClickerBadge(UserStat userStat);

    //일주일간 반응버튼 15회 이상
    boolean checkFeedbackChampionBadge(UserStat userStat);

    //친구 페이지 댓글 3개 이상
    boolean checkCommentFairyBadge(UserStat userStat);

    //하루에 응원해요 3번 이상
    boolean checkCheerMasterBadge(UserStat userStat);

    //하루에 분발해요 버튼 3회 이상
    boolean checkReactionExpertBadge(UserStat userStat);

    //특정 목표 7일 연속 기록
    boolean checkStartOfChallengeBadge(UserStat userStat);

    //누적 30회 기록
    boolean checkDiligentTrackerBadge(UserStat userStat);

    //하루에 3개 이상의 목표 기록
    boolean checkRoutinerBadge(UserStat userStat);

    //3개 이상의 목표를 처음으로 100% 완수한 날
    boolean checkImmersionDayBadge(UserStat userStat);

    //5개 이상 목표 생성
    boolean checkGoalCollectorBadge(UserStat userStat);

    //알림 게시
    boolean checkNotificationStarterBadge(UserStat userStat);

    //분석가
    boolean checkAnalystBadge(UserStat userStat);

    //꾸준한 기록가
    boolean checkConsistentRecorderBadge(UserStat userStat);
}
