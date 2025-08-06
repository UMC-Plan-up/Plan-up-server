package com.planup.planup.domain.report.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.ReportException;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.service.UserGoalService;
import com.planup.planup.domain.report.converter.GoalReportConverter;
import com.planup.planup.domain.report.dto.GoalReportResponseDTO;
import com.planup.planup.domain.report.entity.DailyAchievementRate;
import com.planup.planup.domain.report.entity.GoalReport;
import com.planup.planup.domain.report.repository.GoalReportRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.verification.entity.TimerVerification;
import com.planup.planup.domain.verification.service.PhotoVerificationService;
import com.planup.planup.domain.verification.service.TimerVerificationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class GoalReportServiceImpl implements GoalReportService {

    private final GoalReportRepository goalReportRepository;
    private final UserGoalService userGoalService;
    private final PhotoVerificationService photoVerificationService;
    private final TimerVerificationService timerVerificationService;

    private final int PHOTO_INT = 5;
    private final int TIME_INT = 1000;


    @Override
    public GoalReportResponseDTO.GoalReportResponse findByGoalId(Long id) {
        GoalReport goalReport = goalReportRepository.findById(id).orElseThrow(() -> new ReportException(ErrorStatus.NOT_FOUND_GOAL_REPORT));

        return GoalReportConverter.toResponse(goalReport);
    }

    public void createGoalReport(UserGoal userGoal, LocalDateTime startDate) {
        User user = userGoal.getUser();
        Goal goal = userGoal.getGoal();



        GoalReport.builder()
                .goalId(goal.getId())
                .achievementRate()
                .goalTitle(goal.getGoalName())
                .goalCriteria(goal.getGoalAmount())
                .reportUser()
                .reportType(goal.getGoalType())
                .weeklyReport(null)
                .build();

        if (goal.getVerificationType().equals(VerificationType.PHOTO)) {
        }
    }

    private DailyAchievementRate calculateVerification(List<PhotoVerification> photoVerificationList, UserGoal userGoal, Goal goal, LocalDateTime startDate) {
        DailyAchievementRate.DailyAchievementRateBuilder builder = DailyAchievementRate.builder();

        //날짜별 인증을 저장한다
        Map<LocalDate, Integer> dailyCount = new HashMap<>();

        LocalDateTime endDate = startDate.plusDays(6);

        //각 케이스에 따라 값을 불러온다
        if (goal.getVerificationType().equals(VerificationType.PHOTO)) {
            calculatePhotoVerification(userGoal, dailyCount, startDate, endDate);
        } else if (goal.getVerificationType().equals(VerificationType.TIMER)) {
            calculateTimeVerification(userGoal, dailyCount, startDate, endDate);
        }

        // 날짜별 성취도 계산
        DailyAchievementRate dailyAchievementRate = getDailyAchievementRate(builder, dailyCount);
        return dailyAchievementRate;
    }

    private DailyAchievementRate getDailyAchievementRate(DailyAchievementRate.DailyAchievementRateBuilder builder, Map<LocalDate, Integer> dailyCount) {
        for (Map.Entry<LocalDate, Integer> entry : dailyCount.entrySet()) {
            LocalDate date = entry.getKey();
            int totalPhotoCount = entry.getValue();

            int achievement = (int) Math.min(100, ((double) totalPhotoCount / PHOTO_INT) * 100);

            switch (date.getDayOfWeek()) {
                case MONDAY -> builder.mon(achievement);
                case TUESDAY -> builder.tue(achievement);
                case WEDNESDAY -> builder.wed(achievement);
                case THURSDAY -> builder.thu(achievement);
                case FRIDAY -> builder.fri(achievement);
                case SATURDAY -> builder.sat(achievement);
                case SUNDAY -> builder.sun(achievement);
            }

        }
        DailyAchievementRate dailyAchievementRate = builder.build();
        return dailyAchievementRate;
    }

    private Map<LocalDate, Integer> calculatePhotoVerification(UserGoal userGoal, Map<LocalDate, Integer> dailyPhotoCount, LocalDateTime startDate, LocalDateTime endDate) {
        List<PhotoVerification> verifications = photoVerificationService.getPhotoVerificationListByUserAndDateBetween(userGoal, startDate, endDate);

        // 날짜별 인증 수 카운팅
        for (PhotoVerification photoVerification : verifications) {
            LocalDate date = photoVerification.getCreatedAt().toLocalDate();

            int photoCount = photoVerification.getPhotoImgs() != null ? photoVerification.getPhotoImgs().size() : 0;

            //기존에 데이터가 있으면 불러와서 더한다.
            dailyPhotoCount.put(date, dailyPhotoCount.getOrDefault(date, 0) + photoCount);
        }
        return dailyPhotoCount;
    }

    private Map<LocalDate, Integer> calculateTimeVerification(UserGoal userGoal, Map<LocalDate, Integer> dailyCount, LocalDateTime startDate, LocalDateTime endDate) {
        List<TimerVerification> verifications = timerVerificationService.getTimerVerificationListByUserAndDateBetween(userGoal, startDate, endDate);

        for (TimerVerification verification : verifications) {
            LocalDate date = verification.getCreatedAt().toLocalDate();

            int seconds = (int) (verification.getSpentTime() != null ? verification.getSpentTime().toMillis() / 1000.0 : 0.0);

            dailyCount.put(date, dailyCount.getOrDefault(date, 0) + seconds);
        }
        return dailyCount;
    }
}
