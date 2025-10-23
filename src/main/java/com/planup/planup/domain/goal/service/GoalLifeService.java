package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.repository.GoalRepository;
import com.planup.planup.domain.notification.service.NotificationCreateService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalLifeService {

    private final GoalRepository goalRepository;
    @Lazy
    private final ChallengeService challengeService;
    private final NotificationCreateService notificationCreateService;

    @Transactional
    public void disableExpiredGoals(Date date) {
        List<Goal> goal = goalRepository.findByEndDateBeforeAndIsActiveTrue(date);
        goal.forEach(Goal::setInActive);

        //만약 챌린지라면
        goal.stream()
                .filter(Goal::isChallenge)
                .map(g -> (Challenge) g)
                .forEach(notificationCreateService::createChallengeNotification);
    }
}


