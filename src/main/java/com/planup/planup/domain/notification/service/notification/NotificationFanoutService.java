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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationFanoutService {

    private final NotificationCommandService notificationService;
    private final UserQueryService userQueryService;

    public void createChallengeEndNotification(Challenge challenge) {
        List<UserGoal> userGoals = challenge.getUserGoals();

        if (userGoals == null) {
            return;
        }

        List<Long> nicknameList = new ArrayList<>();

        for (UserGoal ug : userGoals) {
            nicknameList.add(ug.getUser().getId());
        }

        notificationService.createNotification(nicknameList.get(0), nicknameList.get(1),
                NotificationType.CHALLENGE_ENDED, TargetType.CHALLENGE, challenge.getId(), null);
        notificationService.createNotification(nicknameList.get(1), nicknameList.get(0),
                NotificationType.CHALLENGE_ENDED, TargetType.CHALLENGE, challenge.getId(), null);

    }

    public void createChallengeStartNoti(User user, User friend, Challenge challenge) {

        if (challenge.isRePenalty()) {
            notificationService.createNotification(friend.getId(), user.getId(), NotificationType.PENALTY_ACCEPTED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);

        } else {
            notificationService.createNotification(friend.getId(), user.getId(), NotificationType.CHALLENGE_REQUEST_ACCEPTED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);
        }

        notificationService.createNotification(user.getId(), friend.getId(), NotificationType.CHALLENGE_STARTED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);
        notificationService.createNotification(friend.getId(), user.getId(), NotificationType.CHALLENGE_STARTED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);

    }

    public void createGoalCreatedNotification(Long creatorId, Long goalId) {
        List<User> friends = userQueryService.getFriendsByUserId(creatorId);

        for (User friend : friends) {
            notificationService.createNotification(
                    friend.getId(),
                    creatorId,
                    NotificationType.FRIEND_GOAL_CREATED,
                    TargetType.GOAL,
                    goalId,
                    NotificationGroup.SERVICE
            );
        }
    }

    public void createChallengeRequestSentAndReceive(Long creator, Long target, Challenge challenge) {
        notificationService.createNotification(creator, target, NotificationType.CHALLENGE_REQUEST_SENT, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);
        notificationService.createNotification(target, creator, NotificationType.CHALLENGE_REQUEST_RECEIVED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);
    }

    public void createChallengeRejected(Long creator, Long target, Challenge challenge) {
        if (challenge.isRePenalty()) {
            notificationService.createNotification(target, creator, NotificationType.PENALTY_REJECTED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);
        } else {
            notificationService.createNotification(target, creator, NotificationType.CHALLENGE_REQUEST_REJECTED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);
        }
    }

    public void createRemindPenalty(Long creator, Long target, Challenge challenge) {
        notificationService.createNotification(target, creator, NotificationType.PENALTY_REMINDER_SENT, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);

    }

    public void createReRequestPenalty(Long creator, Long target, Challenge challenge) {
        notificationService.createNotification(creator, target, NotificationType.PENALTY_PROPOSAL_RECEIVED, TargetType.CHALLENGE, challenge.getId(), NotificationGroup.SERVICE);
    }

    //랭킹 자리를 빼앗긴 경우
    //FIXME: 아직 구현 안됨
    public void createByRankingInterrupt(Long sender, Long receiver, Goal goal) {
        notificationService.createNotification(receiver, sender, NotificationType.RANK_DOWN, TargetType.GOAL, goal.getId(), NotificationGroup.SERVICE);
    }

    //목표 실천 알림
    //FIXME: 아직 구현 안됨
    public void createByTimeToDo(Long receiver, Goal goal) {
//        notificationService.createNotification(receiver, )
    }


    //참여자가 목표를 수정한 경우
    public void createdByEditedByParticipant(Long sender, List<Long> receivers, Goal goal) {
        for (Long receiver : receivers) {
            notificationService.createNotification(receiver, sender, NotificationType.GOAL_PART_UPDATED, TargetType.GOAL, goal.getId(), NotificationGroup.SERVICE);
        }
    }

    //댓글단 경우
    public void createdByCreatedComment(Long sender, List<Long> receivers, Comment comment, Goal goal) {
        for (Long receiver : receivers) {
            notificationService.createNotification(receiver, sender, NotificationType.COMMENT_ON_VERIFICATION, TargetType.GOAL, goal.getId(), NotificationGroup.SERVICE);
        }
    }

    //응원해요 받은 경우
    public void createdByReactionCHEERToMyGoal(Long sender, List<Long> receivers, Goal goal) {
        for (Long receiver : receivers) {
            notificationService.createNotification(receiver, sender, NotificationType.FEEDBACK_CHEERED, TargetType.REACTION, goal.getId(), NotificationGroup.SERVICE);
        }
    }

    //분발해요 받은 경우
    public void createdByReactionENCOURAGEToMyGoal(Long sender, List<Long> receivers, Goal goal) {
        for (Long receiver : receivers) {
            notificationService.createNotification(receiver, sender, NotificationType.FEEDBACK_ENCOURAGED, TargetType.REACTION, goal.getId(), NotificationGroup.SERVICE);
        }
    }

}
