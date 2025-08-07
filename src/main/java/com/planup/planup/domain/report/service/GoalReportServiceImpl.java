package com.planup.planup.domain.report.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.ReportException;
import com.planup.planup.domain.global.redis.RedisServiceForReport;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.service.UserGoalService;
import com.planup.planup.domain.report.converter.GoalReportConverter;
import com.planup.planup.domain.report.dto.GoalReportResponseDTO;
import com.planup.planup.domain.report.entity.*;
import com.planup.planup.domain.report.repository.GoalReportRepository;
import com.planup.planup.domain.report.repository.ReportUserRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.verification.entity.TimerVerification;
import com.planup.planup.domain.verification.service.PhotoVerificationService;
import com.planup.planup.domain.verification.service.TimerVerificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Slf4j
public class GoalReportServiceImpl implements GoalReportService {

    private final GoalReportRepository goalReportRepository;
    private final UserGoalService userGoalService;
    private final PhotoVerificationService photoVerificationService;
    private final TimerVerificationService timerVerificationService;
    private final RedisServiceForReport redisServiceForReport;
    private final ReportUserRepository reportUserRepository;

    private final int PHOTO_INT = 5;
    private final int TIME_INT = 1000;

    @Override
    public void createGoalReportsByUserGoal(LocalDateTime startDate, LocalDateTime endDate) {

        //이번주에 userGoal에 업데이트가 있었던 목표에 대해 대상이 된다.
        List<UserGoal> userGoalList = userGoalService.getUserGoalInPeriod(startDate, endDate);

        //리포트를 생성하고 개인의 종합 점수를 Redis에 저장한다.
        for (UserGoal userGoal : userGoalList) {
            createGoalReport(userGoal, startDate);
        }

        //Redis에 저장된 개인 종합 당성률을 기반으로 친구 데이터를 만들고 리포트에 추가한다.
        for (UserGoal userGoal : userGoalList) {
            createReportUsersFromRedis(userGoal, startDate);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GoalReportResponseDTO.GoalReportResponse findDTOById(Long id) {
        GoalReport goalReport = goalReportRepository.findById(id).orElseThrow(() -> new ReportException(ErrorStatus.NOT_FOUND_GOAL_REPORT));
        return GoalReportConverter.toResponse(goalReport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalReport> findByGoalIdRecent2(Long id) {
        return goalReportRepository.findTop2ByGoalIdOrderByIdDesc(id);
    }

    @Override
    @Transactional
    public void createGoalReport(UserGoal userGoal, LocalDateTime startDate) {
        User user = userGoal.getUser();
        Goal goal = userGoal.getGoal();

        Integer userValue = redisServiceForReport.getUserValue(user.getId().toString(), goal.getId().toString());

        //dailyAchievementRate 계산
        DailyAchievementRate dailyAchievementRate = calculateVerification(userGoal, goal, startDate);

        //Redis에 나의 값을 저장
        int thisWeekAchRate = dailyAchievementRate.getTotal();
        redisServiceForReport.saveUserValue(user.getId().toString(), goal.getId().toString(), thisWeekAchRate);

        //GoalType을 ReportType으로 수정
        ReportType rp = getReportType(goal);

        ThreeWeekAchievementRate threeWeekAchievementRate = createThreeWeekAchievementRate(thisWeekAchRate, userGoal, startDate);

        GoalReport goalReport = GoalReport.builder()
                .goalId(goal.getId())
                .goalTitle(goal.getGoalName())
                .goalCriteria(goal.getGoalAmount())
                .dailyAchievementRate(dailyAchievementRate)
                .threeWeekAhcievementRate(threeWeekAchievementRate)
                .reportUsers(null)
                .reportType(rp)
                .weeklyReport(null)
                .build();
        GoalReport savedReport = goalReportRepository.save(goalReport);
        redisServiceForReport.saveUserReport(user.getId().toString(), goal.getId().toString(), savedReport.getId());
    }

    private static ReportType getReportType(Goal goal) {
        ReportType rp = null;
        if (goal.getGoalType().equals(GoalType.CHALLENGE_PHOTO) || goal.getGoalType().equals(GoalType.CHALLENGE_PHOTO)) {
            rp = ReportType.CHALLENGE;
        } else if (goal.getGoalType().equals(GoalType.FRIEND)) {
            rp = ReportType.FRIEND;
        } else if (goal.getGoalType().equals(GoalType.COMMUNITY)) {
            rp = ReportType.COMMUNITY;
        }
        return rp;
    }

    @Override
    @Transactional
    public void createReportUsersFromRedis(UserGoal userGoal, LocalDateTime startDate) {
        User user = userGoal.getUser();
        Goal goal = userGoal.getGoal();

        Integer userReport = redisServiceForReport.getUserReport(user.getId().toString(), goal.getId().toString());
        GoalReport goalReport = goalReportRepository.findById(userReport.longValue()).orElseThrow(() -> new RuntimeException("createReportUsersFromRedis: report가 없어요"));

        List<UserGoal> userGoals = userGoalService.getUserGoalListByGoal(goal);
        List<ReportUser> reportUsers = new ArrayList<>();

        //이 목표에 연관된 모든 사람의 데이터를 가져온다
        for (UserGoal userGoalA : userGoals) {

            User userA = userGoalA.getUser();

            //만약 본인의 데이터라면 패스
            if (userA.getId().equals(user.getId())) {
                continue;
            }

            Integer userValue = redisServiceForReport.getUserValue(userA.getId().toString(), goal.getId().toString());

            //데이터가 없다면 패스
            if (userValue == null) {
                continue;
            }

            ReportUser reportUser = ReportUser.builder()
                    .userName(userA.getNickname())
                    .rate(userValue)
                    .goalReport(goalReport)
                    .build();
            reportUserRepository.save(reportUser);
            reportUsers.add(reportUser);
        }
        goalReport.setReportUsers(reportUsers);
    }

    private ThreeWeekAchievementRate createThreeWeekAchievementRate(int thisWeekRate, UserGoal userGoal, LocalDateTime thisWeek) {
        List<GoalReport> goalReportList = findByGoalIdRecent2(userGoal.getGoal().getId());

        LocalDate oneWeekBefore = thisWeek.minusWeeks(1).with(DayOfWeek.MONDAY).toLocalDate();
        LocalDate twoWeeksBefore = thisWeek.minusWeeks(2).with(DayOfWeek.MONDAY).toLocalDate();

        Optional<GoalReport> first = goalReportList.stream()
                .filter(r -> r.getCreatedAt().toLocalDate().equals(oneWeekBefore)).findFirst();

        Optional<GoalReport> second = goalReportList.stream()
                .filter(r -> r.getCreatedAt().toLocalDate().equals(twoWeeksBefore)).findFirst();

        ThreeWeekAchievementRate threeWeekAchievementRate = ThreeWeekAchievementRate.builder()
                .thisWeek(thisWeekRate)
                .oneWeekBefore(first.map(report -> report.getDailyAchievementRate().getTotal()).orElse(0))
                .twoWeekBefore(second.map(goalReport -> goalReport.getDailyAchievementRate().getTotal()).orElse(0))
                .build();
        return threeWeekAchievementRate;
    }

    //각 인증을 취합하여 DailyAchievementRate를 생성한다.
    private DailyAchievementRate calculateVerification(UserGoal userGoal, Goal goal, LocalDateTime startDate) {
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
        dailyAchievementRate.calTotal();
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
