package com.planup.planup.domain.notification.service;

import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.notification.entity.NotificationType;
import com.planup.planup.domain.notification.entity.TargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationCreateService {

    private final NotificationService notificationService;

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
}
