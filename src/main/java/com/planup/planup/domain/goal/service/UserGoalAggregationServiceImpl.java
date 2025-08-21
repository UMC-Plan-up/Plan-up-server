package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.friend.service.FriendService;
import com.planup.planup.domain.goal.convertor.GoalConvertor;
import com.planup.planup.domain.goal.convertor.UserGoalConvertor;
import com.planup.planup.domain.goal.dto.GoalResponseDto;
import com.planup.planup.domain.goal.dto.UserGoalResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserGoalAggregationServiceImpl implements UserGoalAggregationService {

    private final UserGoalService userGoalService;
    private final FriendService friendService;
    private final UserGoalCacheService userGoalCacheService;

    @Override
    public GoalResponseDto.DailyAchievementDto getDailyAchievement(Long userId, LocalDate targetDate) {

        Integer dailyAchievement = userGoalCacheService.getDailyAchievement(userId.toString());

        if (dailyAchievement == null) {
            dailyAchievement = userGoalService.calculateDailyAchievement(userId, targetDate);
            userGoalCacheService.saveUserDailyAchievement(userId.toString(), dailyAchievement);
        }

        return GoalConvertor.toDailyAchievementDto(targetDate, dailyAchievement);
    }

    @Override
    public UserGoalResponseDto.GoalTotalAchievementDto getTotalAchievement(Long goalId, Long userId) {

        Integer totalAchievementGoal = userGoalCacheService.getTotalAchievementGoal(goalId.toString(), userId.toString());

        if (totalAchievementGoal == null) {
            UserGoalResponseDto.GoalTotalAchievementDto dto = userGoalService.calculateGoalTotalAchievement(goalId, userId);
            userGoalCacheService.saveTotalAchievementGoal(userId.toString(), goalId.toString(), dto.getTotalAchievementRate());
            return dto;
        }

        return UserGoalConvertor.toGoalTotalAchievementDto(goalId, totalAchievementGoal);
    }

    @Override
    public UserGoalResponseDto.GoalTotalAchievementDto getFriendGoalTotalAchievement(Long userId, Long goalId, Long friendId) {
        friendService.isFriend(userId, friendId);

        Integer totalAchievementGoal = userGoalCacheService.getTotalAchievementGoal(goalId.toString(), friendId.toString());

        if (totalAchievementGoal == null) {
            UserGoalResponseDto.GoalTotalAchievementDto dto = userGoalService.calculateGoalTotalAchievement(goalId, friendId);
            userGoalCacheService.saveTotalAchievementGoal(friendId.toString(), goalId.toString(), dto.getTotalAchievementRate());
            return dto;
        }

        return UserGoalConvertor.toGoalTotalAchievementDto(goalId, totalAchievementGoal);
    }
}
