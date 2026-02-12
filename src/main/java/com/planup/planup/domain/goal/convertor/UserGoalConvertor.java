package com.planup.planup.domain.goal.convertor;

import com.planup.planup.domain.goal.dto.CommunityResponseDto;
import com.planup.planup.domain.goal.dto.UserGoalResponseDto;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.dto.UserDailySummaryDTO;
import com.planup.planup.domain.user.dto.UserProfileDTO;
import com.planup.planup.domain.user.entity.User;

import java.time.LocalDate;
import java.util.List;

public class UserGoalConvertor {
    public static CommunityResponseDto.JoinGoalResponseDto toJoinGoalResponseDto(
            UserGoal userGoal, Goal goal, User user) {

        return CommunityResponseDto.JoinGoalResponseDto.builder()
                .goalId(goal.getId())
                .goalTitle(goal.getGoalName())
                .userId(user.getId())
                .status(userGoal.getStatus())
                .goalType(goal.getGoalType())
                .build();
    }

    public static UserGoalResponseDto.GoalTotalAchievementDto toGoalTotalAchievementDto(
            Long goalId,
            int totalAchievementRate) {

        return UserGoalResponseDto.GoalTotalAchievementDto.builder()
                .goalId(goalId)
                .totalAchievementRate(totalAchievementRate)
                .build();
    }


    public static UserGoalResponseDto.TimerGoalAchievementWithFriendDto toAchievementWithFriend(
            Goal goal,
            List<UserDailySummaryDTO> list,
            LocalDate date,
            long totalSeconds,
            boolean isAchievement

    ) {
        return UserGoalResponseDto.TimerGoalAchievementWithFriendDto.builder()
                .date(date)
                .totalSeconds(totalSeconds)
                .photoUrl(null)
                .goalName(goal.getGoalName())
                .isAchievement(isAchievement)
                .todayMemo(null)
                .frequency(goal.getFrequency())
                .goalAmount(goal.getGoalAmount())
                .friendInfoList(list)
                .build();
    }

}
