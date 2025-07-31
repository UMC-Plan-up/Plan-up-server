package com.planup.planup.domain.goal.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserGoalException;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserGoalServiceImpl implements UserGoalService{

    private final UserGoalRepository userGoalRepository;

    @Override
    public UserGoal getUserGoalByUserAndGoal(User user, Goal goal) {
        return userGoalRepository.findByUserAndGoal(user, goal).orElseThrow(() -> new UserGoalException(ErrorStatus.NOT_FOUND_USERGOAL));
    }

    @Override
    public List<UserGoal> getUserGoalListByGoal(Goal goal) {
        return userGoalRepository.findAllByGoal(goal);
    }
}
