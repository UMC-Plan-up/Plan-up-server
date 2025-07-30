package com.planup.planup.domain.report.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.ReportException;
import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.notification.service.NotificationService;
import com.planup.planup.domain.report.converter.WeeklyReportResponseConverter;
import com.planup.planup.domain.report.dto.WeeklyReportResponseDTO;
import com.planup.planup.domain.report.entity.WeeklyReport;
import com.planup.planup.domain.report.repository.WeeklyReportRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserBadge;
import com.planup.planup.domain.user.service.UserBadgeService;
import com.planup.planup.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WeeklyReportServiceImpl implements WeeklyReportService {

    private final UserService userService;
    private final UserBadgeService userBadgeService;
    private final NotificationService notificationService;
    private final WeeklyReportRepository weeklyReportRepository;


    @Override
    public List<Integer> searchWeeklyReport(Long userId, int year, int month) {
        User user = userService.getUserbyUserId(userId);

        ArrayList<Integer> weeks = new ArrayList<>();
        List<WeeklyReport> reports = weeklyReportRepository.findByUserAndYearAndMonth(user, year, month);
        for (WeeklyReport report : reports) {
            weeks.add(report.getWeekNumber());
        }

        return weeks;
    }

    @Override
    public WeeklyReportResponseDTO.achievementResponse getWeeklyGoalAchievements(Long userId) {
        User user = userService.getUserbyUserId(userId);

        List<Notification> notificationList = notificationService.getTop5RecentByUser(userId);
        List<UserBadge> userBadgeList = userBadgeService.getTop5Recent(user);

        List<BadgeType> badges = userBadgeList.stream().map(UserBadge::getBadgeType).toList();

        return WeeklyReportResponseConverter.toAchievementDTO(badges, notificationList);

    }

    @Override
    public WeeklyReportResponseDTO.WeeklyReportResponse getWeeklyReport(Long userId, int year, int month, int week) {
        User user = userService.getUserbyUserId(userId);

        WeeklyReport weeklyReport = weeklyReportRepository.findByUserAndYearAndMonthAndWeekNumber(user, year, month, week).orElseThrow(() -> new ReportException(ErrorStatus.NOT_FOUND_WEEKLY_REPORT));
        List<BadgeType> badges = userBadgeService.getBadgeInPeriod(weeklyReport.getUser(), weeklyReport.getStartDate(), weeklyReport.getEndDate());

        return WeeklyReportResponseConverter.toWeeklyReportResponse(weeklyReport, badges);
    }
}
