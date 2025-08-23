package com.planup.planup.domain.notification.service;

import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.notification.entity.NotificationType;
import com.planup.planup.domain.notification.entity.TargetType;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationCreateService {

    private final NotificationService notificationService;
    private final UserService userService;
    public void createChallengeNotification(Challenge challenge) {
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
            notificationService.createNotification(friend.getId(), user.getId(), NotificationType.PENALTY_ACCEPTED, TargetType.CHALLENGE, challenge.getId());

        } else {
            notificationService.createNotification(friend.getId(), user.getId(), NotificationType.CHALLENGE_REQUEST_ACCEPTED, TargetType.CHALLENGE, challenge.getId());
        }

        notificationService.createNotification(user.getId(), friend.getId(), NotificationType.CHALLENGE_STARTED, TargetType.CHALLENGE, challenge.getId());
        notificationService.createNotification(friend.getId(), user.getId(), NotificationType.CHALLENGE_STARTED, TargetType.CHALLENGE, challenge.getId());

    }

    public void createGoalCreatedNotification(Long creatorId, Long goalId) {
        List<User> friends = userService.getFriendsByUserId(creatorId);

        for (User friend : friends) {
            notificationService.createNotification(
                    friend.getId(),
                    creatorId,
                    NotificationType.FRIEND_GOAL_CREATED,
                    TargetType.GOAL,
                    goalId
            );
        }
    }
}
