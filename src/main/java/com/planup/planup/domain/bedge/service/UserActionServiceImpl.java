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

    //인증을 추가
    public void recordVerification(Goal goal, User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.recordVerification(goal, user);
        badgeService.checkBadgeOnRecord(userStat);
    }

    //댓글 추가
    public void recordComment(User user, boolean isFriend) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.recordComment(isFriend);
        badgeService.checkBadgeOnComment(userStat, isFriend);
    }

    //특정 목표를 100% 달성함
    public void recordAchieveGoal(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.achieveGoal();
        badgeService.checkImmersionDayBadge(userStat);
    }

    //목표를 생성
    public void recordCreateGoal(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.createGoal();
        badgeService.checkGoalCollectorBadge(userStat);
    }

    //리액션 버튼 누름
    public void recordReaction(User user, ReactionType type) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.addReaction(type);
        badgeService.checkBadgeOnReaction(userStat);
    }

    //푸시 알림을 통해 앱에 들어옴
    public void recordInPush(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.getInByPush();
        badgeService.checkNotificationStarterBadge(userStat);
    }


    //초대 코드 공유
    public void recordShareInviteCode(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.shareInviteCode();
        badgeService.checkBadgeOnInviteCode(userStat);
    }

    //내 초대 코드를 통해 들어옴
    public void recordAcceptInviteCode(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.acceptInviteCode();
        badgeService.checkMagnetUserBadge(userStat);
    }

    //친구 요청
    public void recordRequestFriend(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.requestFriend();
        badgeService.checkFriendlyMaxBadge(userStat);
    }

    //친구 프로필 선택
    public void recordClickFriendProfile(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.clickFriendProfile();
        badgeService.checkProfileClickerBadge(userStat);
    }

    //리포트 읽기
    public void recordReadReport(User user) {
        UserStat userStat = userStatService.findByUserId(user.getId());

        userStat.readReport();
        badgeService.checkAnalystBadge(userStat);
    }
}
