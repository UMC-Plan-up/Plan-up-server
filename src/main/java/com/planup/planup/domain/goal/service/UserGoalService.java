package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.goal.dto.UserGoalResponseDto;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.dto.CommunityResponseDto;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.entity.User;
import org.springframework.cglib.core.Local;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface UserGoalService {
    //Query Service
    UserGoal getUserGoalByUserAndGoal(User user, Goal goal);
    List<UserGoal> getUserGoalListByGoal(Goal goal);

    VerificationType checkVerificationType(UserGoal userGoal);

    //Command Service
    CommunityResponseDto.JoinGoalResponseDto joinGoal(Long userId, Long goalId);

    List<UserGoal> getUserGoalInPeriod(LocalDateTime startDate, LocalDateTime endDate);

    @Transactional(readOnly = true)
    UserGoal getByGoalIdAndUserId(Long goalId, Long userId);

    @Transactional(readOnly = true)
    boolean existUserGoal(Long goalId, Long userId);

    int calculateDailyAchievement(Long userId, LocalDate targetDate);
    UserGoalResponseDto.GoalTotalAchievementDto calculateGoalTotalAchievement(Long goalId, Long userId);
    List<UserGoal> getActiveUserGoalsByUser(Long userId, LocalDate targetDate);

    @Transactional(readOnly = true)
    Integer getUserGoalCount(Long userId);
}
