package com.planup.planup.domain.goal.service;

import com.planup.planup.domain.friend.service.FriendReadService;
import com.planup.planup.domain.global.redis.CacheAggregation;
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
    private final FriendReadService friendService;
    private final CacheAggregation cacheAggregation;

    @Override
    public GoalResponseDto.DailyAchievementDto getDailyAchievement(Long userId, LocalDate targetDate) {

        String key = generateKey(userId.toString(), targetDate.toString());

        Integer dailyAchievement = cacheAggregation.getOrCompute(
                key,
                CacheAggregation.ttlUntilMidnight(),
                Integer.class,
                () -> userGoalService.calculateDailyAchievement(userId, targetDate)
        );

        return GoalConvertor.toDailyAchievementDto(targetDate, dailyAchievement);
    }

    @Override
    public UserGoalResponseDto.GoalTotalAchievementDto getTotalAchievement(Long goalId, Long userId) {

        String key = generateKeyUserAndGoal(userId.toString(), goalId.toString());

        Integer totalAchievementGoal = cacheAggregation.getOrCompute(
                key,
                CacheAggregation.ttlUntilMidnight(),
                Integer.class,
                () -> userGoalService.calculateGoalTotalAchievement(goalId, userId).getTotalAchievementRate()
        );

        return UserGoalConvertor.toGoalTotalAchievementDto(goalId, totalAchievementGoal);
    }

    @Override
    public UserGoalResponseDto.GoalTotalAchievementDto getFriendGoalTotalAchievement(Long userId, Long goalId, Long friendId) {
        friendService.isFriend(userId, friendId);

        String key = generateKeyUserAndGoal(friendId.toString(), goalId.toString());

        Integer totalAchievementGoal = cacheAggregation.getOrCompute(
                key,
                CacheAggregation.ttlUntilMidnight(),
                Integer.class,
                () -> userGoalService.calculateGoalTotalAchievement(goalId, friendId).getTotalAchievementRate()
        );

        return UserGoalConvertor.toGoalTotalAchievementDto(goalId, totalAchievementGoal);
    }

    private String generateKey(String userId, String suffix) {
        return "user:" + userId + ":" + suffix;
    }

    private String generateKeyUserAndGoal(String userId, String goalId) {
        return "user:" + userId + ":goal:" + goalId + ":" + "goalAchievement";
    }
}
