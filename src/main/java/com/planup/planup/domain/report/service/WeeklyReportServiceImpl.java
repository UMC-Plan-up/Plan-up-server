package com.planup.planup.domain.report.service;

import com.planup.planup.domain.bedge.entity.Badge;
import com.planup.planup.domain.bedge.service.BadgeService;
import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.notification.service.NotificationService;
import com.planup.planup.domain.report.converter.WeeklyReportResponseConverter;
import com.planup.planup.domain.report.dto.WeeklyRepoortResponseDTO;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserBadge;
import com.planup.planup.domain.user.service.UserBadgeService;
import com.planup.planup.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeeklyReportServiceImpl implements WeeklyReportService {

    private final UserService userService;
    private final BadgeService badgeService;
    private final UserBadgeService userBadgeService;
    private final NotificationService notificationService;


    @Override
    public List<Integer> searchWeeklyReport(int year, int week) {
        return null;
    }

    @Override
    public WeeklyRepoortResponseDTO.achievementResponse getWeeklyReport(Long userId) {
        User user = userService.getUserbyUserId(userId);

        List<Notification> notificationList = notificationService.getTop5RecentByUser(userId);
        List<UserBadge> userBadgeList = userBadgeService.getTop5Recent(user);

        List<Badge> badges = userBadgeList.stream().map(UserBadge::getBadge).toList();

        return WeeklyReportResponseConverter.toAchievementDTO(badges, notificationList);

    }
}
