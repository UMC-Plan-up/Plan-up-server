package com.planup.planup.domain.bedge.service;

import com.planup.planup.domain.bedge.entity.UserStat;
import com.planup.planup.domain.bedge.service.badge.BadgeService;
import com.planup.planup.domain.bedge.service.badge.BadgeServiceImpl;
import com.planup.planup.domain.bedge.service.userstat.UserStatQueryServiceImpl;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.reaction.domain.ReactionType;
import com.planup.planup.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserActionServiceImpl {

    private final UserStatQueryServiceImpl userStatService;
    private final BadgeServiceImpl badgeService;

    public void recordVerification(Goal goal, User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.recordVerification(goal, user);
        badgeService.checkBadgeOnRecord(userStat);
    }

    public void recordComment(User user, boolean isFriend) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.recordComment(isFriend);
        badgeService.checkBadgeOnComment(userStat, isFriend);
    }

    public void recordAchieveGoal(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.achieveGoal();
        badgeService.checkImmersionDayBadge(userStat);
    }

    public void recordCreateGoal(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.createGoal();
        badgeService.checkGoalCollectorBadge(userStat);
    }

    public void recordReaction(User user, ReactionType type) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.addReaction(type);
        badgeService.checkBadgeOnReaction(userStat);
    }

    public void recordInPush(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.getInByPush();
        badgeService.checkNotificationStarterBadge(userStat);
    }

    public void recordShareInviteCode(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.shareInviteCode();
        badgeService.checkBadgeOnInviteCode(userStat);
    }

    public void recordAcceptInviteCode(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.acceptInviteCode();
        badgeService.checkMagnetUserBadge(userStat);
    }

    public void recordRequestFriend(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.requestFriend();
        badgeService.checkFriendlyMaxBadge(userStat);
    }

    public void recordClickFriendProfile(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.clickFriendProfile();
        badgeService.checkProfileClickerBadge(userStat);
    }

    public void recordReadReport(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.readReport();
        badgeService.checkAnalystBadge(userStat);
    }
}
