package com.planup.planup.domain.report.converter;

import com.planup.planup.domain.bedge.entity.Badge;
import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.report.dto.WeeklyRepoortResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class WeeklyReportResponseConverter {

    public static WeeklyRepoortResponseDTO.achievementResponse toAchievementDTO(List<Badge> badges, List<Notification> notifications) {
        List<WeeklyRepoortResponseDTO.badgeDTO> badgeDTOS = toBadgeDTOs(badges);
        List<WeeklyRepoortResponseDTO.NotificationDTO> notificationDTOS = toNotificationDTOs(notifications);

        return WeeklyRepoortResponseDTO.achievementResponse.builder()
                .bedgeDTOList(badgeDTOS)
                .notificationDTOList(notificationDTOS)
                .build();

    }

    public static List<WeeklyRepoortResponseDTO.badgeDTO> toBadgeDTOs(List<Badge> badges) {
        return badges.stream().map(WeeklyReportResponseConverter::toBadgeDto).collect(Collectors.toList());
    }

    public static WeeklyRepoortResponseDTO.badgeDTO toBadgeDto(Badge badge) {
        return WeeklyRepoortResponseDTO.badgeDTO.builder()
                .badgeId(badge.getId())
                .badgeName(badge.getBadgeName())
                .build();
    }

    public static List<WeeklyRepoortResponseDTO.NotificationDTO> toNotificationDTOs(List<Notification> notifications) {
        return notifications.stream().map(WeeklyReportResponseConverter::toNotificationDTO).collect(Collectors.toList());
    }

    public static WeeklyRepoortResponseDTO.NotificationDTO toNotificationDTO(Notification notification) {
        return WeeklyRepoortResponseDTO.NotificationDTO.builder()
                .notificationText(notification.getContent())
                .id(notification.getId())
                .build();
    }
}
