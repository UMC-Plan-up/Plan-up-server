package com.planup.planup.domain.report.service;


import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.ReportException;
import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.service.UserGoalService;
import com.planup.planup.domain.notification.dto.NotificationResponseDTO;
import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.notification.service.NotificationService;
import com.planup.planup.domain.report.converter.DailyRecordConverter;
import com.planup.planup.domain.report.converter.WeeklyReportResponseConverter;
import com.planup.planup.domain.report.dto.WeeklyReportResponseDTO;
import com.planup.planup.domain.report.entity.DailyRecord;
import com.planup.planup.domain.report.entity.GoalMessage;
import com.planup.planup.domain.report.entity.GoalReport;
import com.planup.planup.domain.report.entity.WeeklyReport;
import com.planup.planup.domain.report.repository.WeeklyReportRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserBadge;
import com.planup.planup.domain.user.service.UserBadgeService;
import com.planup.planup.domain.user.service.UserService;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.verification.entity.TimerVerification;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import com.planup.planup.domain.verification.repository.TimerVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class WeeklyReportServiceImpl implements WeeklyReportService {

    private final UserService userService;
    private final UserBadgeService userBadgeService;
    private final UserGoalService userGoalService;
    private final NotificationService notificationService;
    private final WeeklyReportRepository weeklyReportRepository;
    private final GoalReportService goalReportService;
    private final PhotoVerificationRepository photoVerificationRepository;
    private final TimerVerificationRepository timerVerificationRepository;


    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public WeeklyReportResponseDTO.achievementResponse getWeeklyGoalAchievements(Long userId) {
        User user = userService.getUserbyUserId(userId);

        List<NotificationResponseDTO.NotificationDTO> notificationList = notificationService.getTop5RecentByUser(userId);
        List<UserBadge> userBadgeList = userBadgeService.getTop5Recent(user);

        List<BadgeType> badges = userBadgeList.stream().map(UserBadge::getBadgeType).toList();

        return WeeklyReportResponseConverter.toAchievementDTO(badges, notificationList);

    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyReportResponseDTO.WeeklyReportResponse getWeeklyReport(Long userId, int year, int month, int week) {
        User user = userService.getUserbyUserId(userId);

        WeeklyReport weeklyReport = weeklyReportRepository.findByUserAndYearAndMonthAndWeekNumber(user, year, month, week).orElseThrow(() -> new ReportException(ErrorStatus.NOT_FOUND_WEEKLY_REPORT));
        List<BadgeType> badges = userBadgeService.getBadgeInPeriod(weeklyReport.getUser(), weeklyReport.getStartDate(), weeklyReport.getEndDate());

        return WeeklyReportResponseConverter.toWeeklyReportResponse(weeklyReport, badges);
    }

    @Transactional
    @Override
    public void createWeeklyReportsByUserGoal(LocalDateTime startDate, LocalDateTime endDate) {

        //이번주에 userGoal에 업데이트가 있었던 목표에 대해 대상이 된다.
        List<UserGoal> userGoalList = userGoalService.getUserGoalInPeriod(startDate, endDate);

        for (UserGoal userGoal : userGoalList) {
            createWeeklyReport(userGoal.getUser(), startDate);
        }
    }

    @Override
    @Transactional
    public void createWeeklyReport(User user, LocalDateTime startDate) {
        Long userId = user.getId();

        //리포트 작성일을 기준으로 가장 최근 일주일의 값을 지정한다. 이때 작성된 GOALREPORT를 조회
        LocalDateTime startOfDay = startDate.toLocalDate().atStartOfDay(); // 2025-08-07T00:00
        LocalDateTime endOfDay = startDate.toLocalDate().plusDays(1).atStartOfDay().minusNanos(1); // 2025-08-07T23:59:59.999999999

        //weeklyReport에 들어갈 리포트 조회
        List<GoalReport> goalReportList = goalReportService.getListByUserIdOneDay(userId, startOfDay, endOfDay);

        //weeklyReport에 들어갈 레코드 5개 조회
        List<DailyRecord> dailyRecordForWeeklyReport = getDailyRecordForWeeklyReport(user, startDate, endOfDay);

        WeeklyReport weeklyReport = WeeklyReport.builder()
                .user(user) // User 엔티티
                .year(startDate.getYear())
                .month(startDate.getMonthValue())
                .weekNumber(startDate.get(WeekFields.of(Locale.KOREA).weekOfMonth()))
                .startDate(startDate)
                .endDate(endOfDay)
                //TODO: goalMessage 찾아오기
                .nextGoalMessage(GoalMessage.KEEP_GOING) // Enum 값
                .goalReports(goalReportList) // 초기 리스트
                .dailyRecords(dailyRecordForWeeklyReport) // 초기 리스트
                .build();

        weeklyReportRepository.save(weeklyReport);
    }

    private List<DailyRecord> getDailyRecordForWeeklyReport(User user, LocalDateTime start, LocalDateTime end) {
        List<PhotoVerification> photoVerificationList = photoVerificationRepository.findTop5ByUserAndDateRange(user, start, end, PageRequest.of(0, 5));
        List<TimerVerification> timerVerificationList = timerVerificationRepository.findTop5ByUserAndDateRange(user, start, end, PageRequest.of(0, 5));

        List<DailyRecord> pvList = photoVerificationList.stream().map(DailyRecordConverter::PhototoDailyRecord).toList();
        List<DailyRecord> tvList = timerVerificationList.stream().map(DailyRecordConverter::TimerToRecord).toList();

        List<DailyRecord> top5DailyRecords = Stream.concat(pvList.stream(), tvList.stream())
                .sorted(Comparator.comparing(DailyRecord::getVerifiedDate).reversed())
                .limit(5).toList();

        return top5DailyRecords;
    }
}
