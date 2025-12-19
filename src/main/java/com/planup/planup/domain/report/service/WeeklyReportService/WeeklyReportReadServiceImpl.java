package com.planup.planup.domain.report.service.WeeklyReportService;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.ReportException;
import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.global.message.EncouragementService;
import com.planup.planup.domain.global.message.MessageResponse;
import com.planup.planup.domain.notification.dto.NotificationResponseDTO;
import com.planup.planup.domain.report.converter.WeeklyReportResponseConverter;
import com.planup.planup.domain.report.dto.WeeklyReportResponseDTO;
import com.planup.planup.domain.report.entity.WeeklyReport;
import com.planup.planup.domain.report.repository.WeeklyReportRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserBadge;
import com.planup.planup.domain.user.service.query.UserBadgeQueryService;
import com.planup.planup.domain.user.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyReportReadServiceImpl implements WeeklyReportReadService {

    private final UserBadgeQueryService userBadgeQueryService;
    private final NotificationService notificationService;
    private final EncouragementService encouragementService;
    private final UserQueryService userQueryService;
    private final WeeklyReportRepository weeklyReportRepository;


    @Override
    public List<Integer> searchWeeklyReport(Long userId, int year, int month) {
        User user = userQueryService.getUserByUserId(userId);

        ArrayList<Integer> weeks = new ArrayList<>();
        List<WeeklyReport> reports = weeklyReportRepository.findByUserAndYearAndMonth(user, year, month);
        for (WeeklyReport report : reports) {
            weeks.add(report.getWeekNumber());
        }

        return weeks;
    }

    @Override
    public WeeklyReportResponseDTO.achievementResponse getWeeklyGoalAchievements(Long userId) {
        User user = userQueryService.getUserByUserId(userId);

        List<NotificationResponseDTO.NotificationDTO> notificationList = notificationService.getTop5RecentByUser(userId);
        List<UserBadge> userBadgeList = userBadgeQueryService.getTop5Recent(user);

        List<BadgeType> badges = userBadgeList.stream().map(UserBadge::getBadgeType).toList();

        Mono<MessageResponse> generate = encouragementService.generate(userId);

        return WeeklyReportResponseConverter.toAchievementDTO(badges, notificationList, generate.block().message());
    }

    @Override
    public WeeklyReportResponseDTO.WeeklyReportResponse getWeeklyReport(Long userId, int year, int month, int week) {
        User user = userQueryService.getUserByUserId(userId);

        WeeklyReport weeklyReport = weeklyReportRepository.findByUserAndYearAndMonthAndWeekNumber(user, year, month, week).orElseThrow(() -> new ReportException(ErrorStatus.NOT_FOUND_WEEKLY_REPORT));
        List<BadgeType> badges = userBadgeQueryService.getBadgeInPeriod(weeklyReport.getUser(), weeklyReport.getStartDate(), weeklyReport.getEndDate());

        return WeeklyReportResponseConverter.toWeeklyReportResponse(weeklyReport, badges);
    }
}
