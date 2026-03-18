package com.planup.planup.domain.notification.service.notification;

import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.notification.entity.notification.NotificationGroup;
import com.planup.planup.domain.notification.entity.notification.NotificationType;
import com.planup.planup.domain.notification.entity.notification.TargetType;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationFanoutService {

    private final NotificationCommandService notificationService;
    private final UserQueryService userQueryService;

    /**
     * 친구의 신청/수락/거절과 관련된 메서드 들은 FriendListener에 있음
     */

    public void createChallengeEndNotification(Challenge challenge) {
        List<UserGoal> userGoals = challenge.getUserGoals();

        if (userGoals == null || userGoals.size() < 2) {
            log.warn("Skip challenge end notification. invalid participants. challengeId={}, participantCount={}",
                    challenge.getId(),
                    userGoals == null ? null : userGoals.size());
            return;
        }

        Long user1 = userGoals.get(0).getUser().getId();
        Long user2 = userGoals.get(1).getUser().getId();

        log.info("Fanout challenge end notification. challengeId={}, user1={}, user2={}", challenge.getId(), user1, user2);

        notificationService.createNotification(user1, user2,
                NotificationType.CHALLENGE_ENDED, TargetType.CHALLENGE, challenge.getId(), null);

        notificationService.createNotification(user2, user1,
                NotificationType.CHALLENGE_ENDED, TargetType.CHALLENGE, challenge.getId(), null);

        log.info("Challenge end notification completed. challengeId={}, receivers=[{}, {}]",
                challenge.getId(), user1, user2);
    }

    public void createChallengeStartNoti(User user, User friend, Challenge challenge) {

        log.info("Fanout challenge start notification. challengeId={}, userId={}, friendId={}, rePenalty={}",
                challenge.getId(), user.getId(), friend.getId(), challenge.isRePenalty());

        if (challenge.isRePenalty()) {
            notificationService.createNotification(friend.getId(), user.getId(), NotificationType.PENALTY_ACCEPTED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);

        } else {
            notificationService.createNotification(friend.getId(), user.getId(), NotificationType.CHALLENGE_REQUEST_ACCEPTED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);
        }

        notificationService.createNotification(user.getId(), friend.getId(), NotificationType.CHALLENGE_STARTED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);
        notificationService.createNotification(friend.getId(), user.getId(), NotificationType.CHALLENGE_STARTED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);

        log.info("Challenge start notification completed. challengeId={}, participants=[{}, {}]",
                challenge.getId(), user.getId(), friend.getId());
    }

    public void createGoalCreatedNotification(Long creatorId, Long goalId) {
        List<User> friends = userQueryService.getFriendsByUserId(creatorId);

        if (friends == null || friends.isEmpty()) {
            log.info("No friends to notify for goal created. creatorId={}, goalId={}", creatorId, goalId);
            return;
        }

        log.info("Fanout goal created notification. creatorId={}, goalId={}, friendCount={}",
                creatorId, goalId, friends.size());

        for (User friend : friends) {
            log.debug("Create goal created notification. creatorId={}, receiverId={}, goalId={}",
                    creatorId, friend.getId(), goalId);

            notificationService.createNotification(
                    friend.getId(),
                    creatorId,
                    NotificationType.FRIEND_GOAL_CREATED,
                    TargetType.GOAL,
                    goalId,
                    NotificationGroup.SERVICE
            );
        }

        log.info("Goal created notification completed. creatorId={}, goalId={}, friendCount={}",
                creatorId, goalId, friends.size());
    }

    public void createChallengeRequestSentAndReceive(Long creator, Long target, Challenge challenge) {

        log.info("Fanout challenge request sent/received. challengeId={}, creatorId={}, targetId={}",
                challenge.getId(), creator, target);

        notificationService.createNotification(creator, target, NotificationType.CHALLENGE_REQUEST_SENT, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);
        notificationService.createNotification(target, creator, NotificationType.CHALLENGE_REQUEST_RECEIVED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);
    }

    public void createChallengeRejected(Long creator, Long target, Challenge challenge) {
        log.info("Fanout challenge rejected. challengeId={}, creatorId={}, targetId={}, rePenalty={}",
                challenge.getId(), creator, target, challenge.isRePenalty());

        if (challenge.isRePenalty()) {
            notificationService.createNotification(target, creator, NotificationType.PENALTY_REJECTED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);
        } else {
            notificationService.createNotification(target, creator, NotificationType.CHALLENGE_REQUEST_REJECTED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);
        }
    }

    public void createRemindPenalty(Long creator, Long target, Challenge challenge) {
        log.info("Fanout penalty reminder. challengeId={}, creatorId={}, targetId={}",
                challenge.getId(), creator, target);

        notificationService.createNotification(target, creator, NotificationType.PENALTY_REMINDER_SENT, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);

    }

    public void createReRequestPenalty(Long creator, Long target, Challenge challenge) {
        log.info("Fanout penalty re-request. challengeId={}, creatorId={}, targetId={}",
                challenge.getId(), creator, target);

        notificationService.createNotification(creator, target, NotificationType.PENALTY_PROPOSAL_RECEIVED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);
    }

    //랭킹 자리를 빼앗긴 경우
    //FIXME: 아직 구현 안됨
    public void createByRankingInterrupt(Long sender, Long receiver, Goal goal) {
        log.info("Fanout rank down notification. senderId={}, receiverId={}, goalId={}",
                sender, receiver, goal.getId());

        notificationService.createNotification(receiver, sender, NotificationType.RANK_DOWN, TargetType.GOAL, goal.getId(), NotificationGroup.SERVICE);
    }

    //목표 실천 알림
    //FIXME: 아직 구현 안됨
    public void createByTimeToDo(Long receiver, Goal goal) {
        log.info("Time-to-do notification called but not implemented. receiverId={}, goalId={}",
                receiver, goal.getId());
//        notificationService.createNotification(receiver, )
    }


    //참여자가 목표를 수정한 경우
    public void createdByEditedByParticipant(Long sender, List<Long> receivers, Goal goal) {
        if (receivers == null || receivers.isEmpty()) {
            log.info("No receivers for goal participant updated notification. senderId={}, goalId={}",
                    sender, goal.getId());
            return;
        }

        log.info("Fanout goal participant updated notification. senderId={}, goalId={}, receiverCount={}",
                sender, goal.getId(), receivers.size());
        log.debug("Receivers for goal participant updated. goalId={}, receivers={}", goal.getId(), receivers);

        for (Long receiver : receivers) {
            notificationService.createNotification(
                    receiver, sender,
                    NotificationType.GOAL_PART_UPDATED, TargetType.GOAL, goal.getId(), NotificationGroup.SERVICE
            );
        }
    }

    //댓글단 경우
    public void createdByCreatedComment(Long sender, List<Long> receivers, Comment comment, Goal goal) {
        if (receivers == null || receivers.isEmpty()) {
            log.info("No receivers for comment notification. senderId={}, goalId={}, commentId={}",
                    sender, goal.getId(), comment.getId());
            return;
        }

        log.info("Fanout comment notification. senderId={}, goalId={}, commentId={}, receiverCount={}",
                sender, goal.getId(), comment.getId(), receivers.size());
        log.debug("Receivers for comment notification. goalId={}, commentId={}, receivers={}",
                goal.getId(), comment.getId(), receivers);

        for (Long receiver : receivers) {
            notificationService.createNotification(
                    receiver, sender,
                    NotificationType.COMMENT_ON_VERIFICATION, TargetType.GOAL, goal.getId(), NotificationGroup.SERVICE
            );
        }
    }

    //응원해요 받은 경우
    public void createdByReactionCHEERToMyGoal(Long sender, List<Long> receivers, Goal goal) {
        if (receivers == null || receivers.isEmpty()) {
            log.info("No receivers for cheer reaction notification. senderId={}, goalId={}",
                    sender, goal.getId());
            return;
        }

        log.info("Fanout cheer reaction notification. senderId={}, goalId={}, receiverCount={}",
                sender, goal.getId(), receivers.size());
        log.debug("Receivers for cheer reaction notification. goalId={}, receivers={}", goal.getId(), receivers);

        for (Long receiver : receivers) {
            notificationService.createNotification(
                    receiver, sender,
                    NotificationType.FEEDBACK_CHEERED, TargetType.REACTION, goal.getId(), NotificationGroup.SERVICE
            );
        }
    }

    //분발해요 받은 경우
    public void createdByReactionENCOURAGEToMyGoal(Long sender, List<Long> receivers, Goal goal) {
        if (receivers == null || receivers.isEmpty()) {
            log.info("No receivers for encourage reaction notification. senderId={}, goalId={}",
                    sender, goal.getId());
            return;
        }

        log.info("Fanout encourage reaction notification. senderId={}, goalId={}, receiverCount={}",
                sender, goal.getId(), receivers.size());
        log.debug("Receivers for encourage reaction notification. goalId={}, receivers={}", goal.getId(), receivers);

        for (Long receiver : receivers) {
            notificationService.createNotification(
                    receiver, sender,
                    NotificationType.FEEDBACK_ENCOURAGED, TargetType.REACTION, goal.getId(), NotificationGroup.SERVICE
            );
        }
    }

}
