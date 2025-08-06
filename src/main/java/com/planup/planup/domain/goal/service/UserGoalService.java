package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.dto.CommunityResponseDto;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.entity.User;

import java.util.List;

public interface UserGoalService {
    //Query Service
    UserGoal getUserGoalByUserAndGoal(User user, Goal goal);
    List<UserGoal> getUserGoalListByGoal(Goal goal);

//    List<UserGoal> getUserGoalListByGoalBetweenDay(User user, LocalDateTime startDate, LocalDateTime endDate);

    VerificationType checkVerificationType(UserGoal userGoal);


    //Command Service
    CommunityResponseDto.JoinGoalResponseDto joinGoal(Long userId, Long goalId);

}
