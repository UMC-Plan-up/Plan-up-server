package com.planup.planup.domain.bedge.event;

import com.planup.planup.domain.bedge.entity.UserStat;
import com.planup.planup.domain.bedge.service.badge.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BadgeEventHandler {

    private final BadgeService badgeService;

    @EventListener
    public void handleUserStatChanged(UserStatChangedEvent event) {
        UserStat userStat = event.getUserStat();
        String methodName = event.getMethodName();

        switch (methodName) {
            case "addSharingInviteCode" -> {
                badgeService.checkInfluentialStarterBadge(userStat);
                badgeService.checkWordOfMouthMasterBadge(userStat);
            }
            case "addAcceptInviteCode" -> badgeService.checkMagnetUserBadge(userStat);
            case "addRequestFriendOneDay" -> badgeService.checkFriendlyMaxBadge(userStat);

            case "addComment" -> badgeService.checkFirstCommentBadge(userStat);
            case "addFriendRequestCnt" -> badgeService.checkFriendRequestKingBadge(userStat);
            case "addClickFriendProfile" -> badgeService.checkProfileClickerBadge(userStat);
            case "addReactionButton" -> badgeService.checkFeedbackChampionBadge(userStat);
            case "addCommentCntInFriendDay" -> badgeService.checkCommentFairyBadge(userStat);
            case "addLikeCnt" -> badgeService.checkCheerMasterBadge(userStat);
            case "addEncourageCnt" -> badgeService.checkReactionExpertBadge(userStat);

            case "addRecordAllGoal7DaysIfNeeded" -> {
                badgeService.checkStartOfChallengeBadge(userStat);
                badgeService.checkDiligentTrackerBadge(userStat);
            }
            case "addVerify" -> {
                badgeService.checkRoutinerBadge(userStat);
                badgeService.checkConsistentRecorderBadge(userStat);
            }
            case "addComplete3Goal" -> badgeService.checkImmersionDayBadge(userStat);

            case "addGoal" -> badgeService.checkGoalCollectorBadge(userStat);
            case "addPushOpenCnt" -> badgeService.checkNotificationStarterBadge(userStat);
            case "addWeeklyStatViewCnt" -> badgeService.checkAnalystBadge(userStat);
        }
    }
}
