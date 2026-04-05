package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalRankingService {

    private final GoalService goalService;
    private final UserQueryService userQueryService;
    private final UserGoalService userGoalService;

    public void getRankInGoal( Long userId, Long goalId) {
        User user = userQueryService.getUserByUserId(userId);
        Goal goal = goalService.getGoalById(goalId);
        UserGoal userGoal = userGoalService.getByGoalIdAndUserId(goalId, userId);



    }

}
