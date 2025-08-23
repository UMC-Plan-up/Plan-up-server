package com.planup.planup.domain.global.message;

import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.notification.dto.NotificationResponseDTO;

import java.util.List;
import java.util.Map;

public record UserData(
    Long userId,
    List<UserGoal> activeGoals,                                    // 활성 목표 목록
    Map<Long, Integer> goalAchievements,                          // 목표별 성취율
    int thisWeekAchievement,                                      // 이번주 성취율
    int lastWeekAchievement,                                      // 지난주 성취율
    List<NotificationResponseDTO.NotificationDTO> recentNotifications, // 최근 알림
    int consecutiveDays,                                          // 연속 성공 일수
    int recentCommentCount,                                       // 최근 받은 댓글 수
    List<String> friendAchievements                               // 친구 성취 목록
) {}
