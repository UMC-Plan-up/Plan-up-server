package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.entity.User;

public interface UserGoalService {
    UserGoal getUserGoalByUserAndGoal(User user, Goal goal);
}
