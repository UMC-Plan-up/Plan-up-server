package com.planup.planup.domain.report.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.ReportException;
import com.planup.planup.domain.global.redis.RedisServiceForReport;
import com.planup.planup.domain.global.service.AchievementCalculationService;
import com.planup.planup.domain.global.service.AfterCommitExecutor;
import com.planup.planup.domain.goal.convertor.CommentConverter;
import com.planup.planup.domain.goal.dto.CommentResponseDto;
import com.planup.planup.domain.goal.entity.Comment;
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
import com.planup.planup.domain.verification.service.PhotoVerificationReadService;
import com.planup.planup.domain.verification.service.TimerVerificationReadService;
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
    private final TimerVerificationReadService timerVerificationReadService;
    private final RedisServiceForReport redisServiceForReport;
    private final ReportUserRepository reportUserRepository;
    private final AchievementCalculationService achievementCalculationService;
    private final PhotoVerificationReadService photoVerificationReadService;
    private final AfterCommitExecutor afterCommitExecutor;

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

    //리포트를 조회한다.
    @Override
    @Transactional(readOnly = true)
    public GoalReport getGoalReportOrThrow(Long id) {
        return goalReportRepository.findById(id)
                .orElseThrow(() -> new ReportException(ErrorStatus.NOT_FOUND_GOAL_REPORT));
    }

    //goalReport와 해당 리포트의 댓글을 조회하여 DTO로 반환
    @Override
    @Transactional(readOnly = true)
    public GoalReportResponseDTO.GoalReportResponse findDTOById(Long id, Long userId) {
        GoalReport goalReport = getGoalReportOrThrow(id);
        List<CommentResponseDto.CommentDto> commentDtoList = getCommentResponseDtoListByGoalReport(goalReport);
        return GoalReportConverter.toGoalReportResponse(goalReport, commentDtoList);
    }

    //리포트에 연결된 코멘트 관련 조회 및 DTO 변환
    private List<CommentResponseDto.CommentDto> getCommentResponseDtoListByGoalReport(GoalReport goalReport) {
        List<Comment> commentList = goalReport.getCommentList();
        return commentList.stream().map(c -> CommentConverter.toResponseDto(c, goalReport.getUserId())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalReport> findTop2RecentByGoalId(Long id) {
        return goalReportRepository.findTop2ByGoalIdOrderByCreatedAt(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalReport> getListByUserIdOneDay(Long userId, LocalDateTime start, LocalDateTime end) {
        return goalReportRepository.findAllByUserIdAndCreatedAtBetween(userId, start, end);
    }

    @Override
    @Transactional
    public void createGoalReport(UserGoal userGoal, LocalDateTime startDate) {
        User user = userGoal.getUser();
        Goal goal = userGoal.getGoal();

        //dailyAchievementRate 계산
        DailyAchievementRate dailyAchievementRate = calculateDailyAchievementRate(userGoal, goal, startDate);
        int thisWeekAchRate = dailyAchievementRate.getTotal();

        //GoalType을 ReportType으로 수정
        ReportType reportType = getReportType(goal);

        ThreeWeekAchievementRate threeWeekAchievementRate = createThreeWeekAchievementRate(thisWeekAchRate, userGoal, startDate);

        GoalReport goalReport = GoalReport.builder()
                .goalId(goal.getId())
                .userId(user.getId())
                .goalTitle(goal.getGoalName())
                .goalCriteria(goal.getGoalAmount())
                .dailyAchievementRate(dailyAchievementRate)
                .threeWeekAhcievementRate(threeWeekAchievementRate)
                .reportUsers(null)
                .reportType(reportType)
                .weeklyReport(null)
                .build();
        GoalReport savedReport = goalReportRepository.save(goalReport);

        //데이터베이스와 Redis 사이의 정합성을 위해 커밋 이후 레디스에 적용
        afterCommitExecutor.run(() -> {
            String userKey = user.getId().toString();
            String goalKey = goal.getId().toString();
            redisServiceForReport.saveUserValue(userKey, goalKey, thisWeekAchRate);
            redisServiceForReport.saveUserReport(userKey, goalKey, savedReport.getId());
        });
    }

    private static ReportType getReportType(Goal goal) {
        ReportType rp = null;
        if (goal.getGoalType().equals(GoalType.CHALLENGE_PHOTO) || goal.getGoalType().equals(GoalType.CHALLENGE_TIME)) {
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

        //날짜 범위로 조회
        LocalDate oneWeekMonday = thisWeek.minusWeeks(1).with(DayOfWeek.MONDAY).toLocalDate();
        LocalDate twoWeekMonday = thisWeek.minusWeeks(2).with(DayOfWeek.MONDAY).toLocalDate();

        LocalDateTime oneStart = oneWeekMonday.atStartOfDay();
        LocalDateTime oneEndEx = oneStart.plusDays(7);
        LocalDateTime twoStart = twoWeekMonday.atStartOfDay();
        LocalDateTime twoEndEx = twoStart.plusDays(7);

        //해당 날짜대로 GoalReport 조회
        GoalReport oneWeekReport = getFirstByIdAndPeriod(userGoal, oneStart, oneEndEx);
        GoalReport twoWeeksReport = getFirstByIdAndPeriod(userGoal, twoStart, twoEndEx);

        return ThreeWeekAchievementRate.builder()
                .thisWeek(thisWeekRate)
                .oneWeekBefore(getDailyAchievementRateOrZero(oneWeekReport))
                .twoWeekBefore(getDailyAchievementRateOrZero(twoWeeksReport))
                .build();
    }

    private int getDailyAchievementRateOrZero(GoalReport goalReport) {
        return Optional.ofNullable(goalReport)
                .map(GoalReport::getDailyAchievementRate)
                .map(DailyAchievementRate::getTotal)
                .orElse(0);
    }

    private GoalReport getFirstByIdAndPeriod(UserGoal userGoal, LocalDateTime oneWeekStart, LocalDateTime oneWeekEnd) {
        return goalReportRepository
                .findFirstByGoalIdAndCreatedAtBetween(
                        userGoal.getGoal().getId(), oneWeekStart, oneWeekEnd
                ).orElse(null);
    }

    //각 인증을 취합하여 DailyAchievementRate를 생성한다.
    public DailyAchievementRate calculateDailyAchievementRate(UserGoal userGoal, Goal goal, LocalDateTime startDate) {

        //날짜별 인증을 저장한다
        Map<LocalDate, Integer> dailyCount;
        LocalDateTime endDate = startDate.plusDays(6);

        //각 케이스에 따라 값을 불러온다
        if (goal.getVerificationType().equals(VerificationType.PHOTO)) {
            dailyCount = photoVerificationReadService.calculateVerification(userGoal, startDate, endDate);
        } else if (goal.getVerificationType().equals(VerificationType.TIMER)) {
            dailyCount = timerVerificationReadService.calculateVerification(userGoal, startDate, endDate);
        } else {
            throw new RuntimeException();
        }
        DailyAchievementRate result = getDailyAchievementRate(dailyCount, goal.getOneDose(), startDate);

        // 날짜별 성취도 계산
        return result;
    }

    private DailyAchievementRate getDailyAchievementRate(Map<LocalDate, Integer> dailyCount, int oneDose, LocalDateTime startDate) {
        Map<LocalDate, Integer> byDay = achievementCalculationService.calcAchievementByDay(dailyCount, oneDose);
        return toDailyAchievementRate(byDay, startDate.toLocalDate());
    }



    //퍼센트를 가지고 DailyAchievementRate를 만든다.
    private DailyAchievementRate toDailyAchievementRate(Map<LocalDate, Integer> byDay, LocalDate weekStartMonday) {
        DailyAchievementRate.DailyAchievementRateBuilder b = DailyAchievementRate.builder();

        // 값이 없으면 0으로 기본값 처리 (필요 시 조정)
        b.mon(byDay.getOrDefault(weekStartMonday, 0));
        b.tue(byDay.getOrDefault(weekStartMonday.plusDays(1), 0));
        b.wed(byDay.getOrDefault(weekStartMonday.plusDays(2), 0));
        b.thu(byDay.getOrDefault(weekStartMonday.plusDays(3), 0));
        b.fri(byDay.getOrDefault(weekStartMonday.plusDays(4), 0));
        b.sat(byDay.getOrDefault(weekStartMonday.plusDays(5), 0));
        b.sun(byDay.getOrDefault(weekStartMonday.plusDays(6), 0));

        DailyAchievementRate dto = b.build();
        dto.calTotal(); // 총합/평균을 DTO 내부에서 계산하도록 유지
        return dto;
    }

    public List<GoalReport> getGoalReportsByUserAndPeriod(Long userId, LocalDateTime start, LocalDateTime end) {
        return goalReportRepository.findAllByUserIdAndCreatedAtBetween(userId, start, end);
    }

}
