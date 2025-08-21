package com.planup.planup.domain.goal.service;

public interface UserGoalCacheService {
    void saveUserDailyAchievement(String userId, int rate);

    Integer getDailyAchievement(String userId);

    void saveTotalAchievementGoal(String userId, String goalId, int rate);

    Integer getTotalAchievementGoal(String userId, String goalId);
}
