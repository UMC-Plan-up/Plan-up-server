package com.planup.planup.domain.report.converter;

import com.planup.planup.domain.bedge.converter.BadgeConverter;
import com.planup.planup.domain.bedge.dto.BadgeResponseDTO;
import com.planup.planup.domain.bedge.entity.Badge;
import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.report.dto.WeeklyRepoortResponseDTO;
import com.planup.planup.domain.report.entity.DailyAchievementRate;
import com.planup.planup.domain.report.entity.DailyRecord;
import com.planup.planup.domain.report.entity.GoalReport;
import com.planup.planup.domain.report.entity.WeeklyReport;

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

    public static WeeklyRepoortResponseDTO.WeeklyReportResponse toWeeklyReportResponse(WeeklyReport weeklyReport, List<Badge> badgeList) {

        return WeeklyRepoortResponseDTO.WeeklyReportResponse.builder()
                .id(weeklyReport.getId())
                .year(weeklyReport.getYear())
                .month(weeklyReport.getMonth())
                .weekNumber(weeklyReport.getWeekNumber())
                .startDate(weeklyReport.getStartDate())
                .endDate(weeklyReport.getEndDate())
                .nextGoalMessage(weeklyReport.getNextGoalMessage() != null ? weeklyReport.getNextGoalMessage().toString() : null)
                .goalReports(toSimpleGoalReports(weeklyReport.getGoalReports()))
                .totalDailyAchievement(calculateTotalDailyAchievement(weeklyReport.getGoalReports()))
                .dailyRecordList(weeklyReport.getDailyRecords().stream()
                        .map(WeeklyReportResponseConverter::toDailyRecordDTO)
                        .collect(Collectors.toList()))
                .badgeList(badgeList.stream()
                        .map(WeeklyReportResponseConverter::badgeToDTO)
                        .collect(Collectors.toList()))
                .build();

    }

    public static List<WeeklyRepoortResponseDTO.WeeklyReportResponse.SimpleGoalReport> toSimpleGoalReports(List<GoalReport> goalReports) {
        if (goalReports == null) return List.of();

        return goalReports.stream()
                .map(gr -> WeeklyRepoortResponseDTO.WeeklyReportResponse.SimpleGoalReport.builder()
                        .id(gr.getId())
                        .goalTitle(gr.getGoalTitle())
                        .goalCriteria(gr.getGoalCriteria())
                        .achievementRate(gr.getAchievementRate())
                        .build())
                .collect(Collectors.toList());
    }

    public static DailyAchievementRate calculateTotalDailyAchievement(List<GoalReport> goalReports) {

        int mon = 0, tue = 0, wed = 0, thu = 0, fri = 0, sat = 0, sun = 0;

        List<DailyAchievementRate> dailyAchievementRateList = goalReports.stream().map(GoalReport::getDailyAchievementRate).collect(Collectors.toList());

        for (DailyAchievementRate dailyAchievementRate : dailyAchievementRateList) {
            mon += dailyAchievementRate.getMon();
            tue += dailyAchievementRate.getTue();
            wed += dailyAchievementRate.getWed();
            thu += dailyAchievementRate.getThu();
            fri += dailyAchievementRate.getFri();
            sat += dailyAchievementRate.getSat();
            sun += dailyAchievementRate.getSun();
        }

        DailyAchievementRate result = DailyAchievementRate.builder()
                .mon(mon)
                .tue(tue)
                .wed(wed)
                .thu(thu)
                .fri(fri)
                .sat(sat)
                .sun(sun)
                .build();

        result.calTotal();

        return result;
    }

    public static WeeklyRepoortResponseDTO.WeeklyReportResponse.DailyRecordDTO toDailyRecordDTO(DailyRecord dailyRecord) {
        return WeeklyRepoortResponseDTO.WeeklyReportResponse.DailyRecordDTO.builder()
                .id(dailyRecord.getId())
                .date(dailyRecord.getDate())
                .recordedTime(dailyRecord.getRecordedTime())
                .photoVerified(dailyRecord.getPhotoVerified())
                .simpleMessage(dailyRecord.getSimpleMessage())
                .build();
    }

    private static List<BadgeResponseDTO.SimpleBadgeDTO> badgesToDTO(List<Badge> badges) {
        return badges.stream().map(BadgeConverter::toSimpleBadgeDTO).collect(Collectors.toList());
    }

    private static BadgeResponseDTO.SimpleBadgeDTO badgeToDTO(Badge badge) {
        return BadgeConverter.toSimpleBadgeDTO(badge);
    }
}
