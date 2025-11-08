package com.planup.planup.domain.goal.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserGoalException;
import com.planup.planup.domain.friend.service.FriendReadService;
import com.planup.planup.domain.goal.dto.UserGoalResponseDto;
import com.planup.planup.domain.goal.dto.UserWithGoalCountDTO;
import com.planup.planup.domain.goal.entity.Enum.GoalPeriod;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.dto.CommunityResponseDto;
import com.planup.planup.domain.goal.convertor.UserGoalConvertor;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.repository.GoalRepository;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.domain.user.service.UserService;
import com.planup.planup.domain.verification.service.PhotoVerificationReadService;
import com.planup.planup.domain.verification.service.TimerVerificationReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserGoalServiceImpl implements UserGoalService{

    private final UserGoalRepository userGoalRepository;
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final FriendReadService friendService;
    private final PhotoVerificationReadService photoVerificationReadService;
    private final TimerVerificationReadService timerVerificationReadService;
    private final UserService userService;

    @Transactional
    public CommunityResponseDto.JoinGoalResponseDto joinGoal(Long userId, Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 목표입니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        if (existUserGoal(goalId, userId)) {
            return UserGoalConvertor.toJoinGoalResponseDto(getUserGoalByUserAndGoal(user, goal), goal, user);
        }

        if (goal.getGoalType() == GoalType.FRIEND) {
            validateFriendRelation(userId, goalId);
        }

        int currentParticipants = userGoalRepository.countByGoalId(goalId);
        if (currentParticipants >= goal.getLimitFriendCount()) {
            throw new RuntimeException("목표 정원이 초과되었습니다.");
        }
//
//        if (goal.getEndDate() != null && goal.getEndDate().before(new Date())) {
//            throw new RuntimeException("이미 종료된 목표입니다.");
//        }

        UserGoal userGoal = UserGoal.builder()
                .user(user)
                .goal(goal)
                .status(Status.MEMBER)
                .isActive(true)
                .isPublic(true)
                .currentAmount(null)
                .verificationCount(0)
                .goalTime(0)
                .build();

        UserGoal savedUserGoal = userGoalRepository.save(userGoal);

        return UserGoalConvertor.toJoinGoalResponseDto(savedUserGoal, goal, user);
    }

    //친구 관계 검증을 위한 헬퍼 메서드
    private void validateFriendRelation(Long userId, Long goalId) {
        UserGoal adminUserGoal = userGoalRepository.findByGoalIdAndStatus(goalId, Status.ADMIN);
        if (adminUserGoal == null) {
            throw new RuntimeException("목표 생성자를 찾을 수 없습니다.");
        }

        Long creatorId = adminUserGoal.getUser().getId();

        friendService.isFriend(userId, creatorId);
    }

    //달성량 계산 파트
    @Transactional(readOnly = true)
    public int calculateDailyAchievement(Long userId, LocalDate targetDate) {
        List<UserGoal> activeUserGoals = getActiveUserGoalsByUser(userId, targetDate);

        if (activeUserGoals.isEmpty()) {
            return 0;
        }

        List<Integer> achievementRates = new ArrayList<>();

        for (UserGoal userGoal : activeUserGoals) {
            int dailyRate = calculateSingleGoalAchievement(userGoal, targetDate);
            achievementRates.add(dailyRate);
        }

        return (int) achievementRates.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    public int calculateSingleGoalAchievement(UserGoal userGoal, LocalDate targetDate) {
        Goal goal = userGoal.getGoal();

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);

        Map<LocalDate, Integer> dailyCount;

        if (goal.getVerificationType().equals(VerificationType.PHOTO)) {
            dailyCount = photoVerificationReadService.calculateVerification(userGoal, startOfDay, endOfDay);
        } else if (goal.getVerificationType().equals(VerificationType.TIMER)) {
            dailyCount = timerVerificationReadService.calculateVerification(userGoal, startOfDay, endOfDay);
        } else {
            return 0;
        }

        int actualCount = dailyCount.getOrDefault(targetDate, 0);

        if (goal.getOneDose() == 0) {
            return 0;
        }

        return Math.min(100, (actualCount * 100) / goal.getOneDose());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserGoal> getActiveUserGoalsByUser(Long userId, LocalDate targetDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        return userGoalRepository.findActiveUserGoalsByUser(user, targetDate);
    }

    @Transactional(readOnly = true)
    public UserGoalResponseDto.GoalTotalAchievementDto calculateGoalTotalAchievement(Long goalId, Long userId) {
        UserGoal userGoal = userGoalRepository.findByGoalIdAndUserId(goalId, userId);

        if (userGoal == null) {
            throw new UserGoalException(ErrorStatus.NOT_FOUND_GOAL);
        }

        Goal goal = userGoal.getGoal();

        LocalDate startDate = userGoal.getCreatedAt().toLocalDate();
        LocalDate endDate = goal.getEndDate();
        LocalDate today = LocalDate.now();

        long elapsedDays = ChronoUnit.DAYS.between(startDate, today.isAfter(endDate) ? endDate : today);
        int expectedCount = calculateExpectedVerifications(goal.getFrequency(), goal.getPeriod(), elapsedDays);

        int actualCount = userGoal.getVerificationCount();
        int achievementRate = expectedCount > 0 ?
                Math.min(100, (actualCount * 100) / expectedCount) : 0;

        return UserGoalConvertor.toGoalTotalAchievementDto(goalId, achievementRate);
    }

    private int calculateExpectedVerifications(int frequency, GoalPeriod period, long days) {
        if (days <= 0) return 0;

        // GoalPeriod 가 null 인 경우
        if (period == null) {
            return 0;
        }

        switch (period) {
            case DAY:
                return (int) (frequency * days);
            case WEEK:
                return (int) Math.ceil(frequency * (days / 7.0));
            case MONTH:
                return (int) Math.ceil(frequency * (days / 30.0));
            default:
                return 0;
        }
    }

    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    //수용 형 파트
    @Override
    public UserGoal getUserGoalByUserAndGoal(User user, Goal goal) {
        return userGoalRepository.findAllByUserAndGoal(user, goal).get(0);
//        return userGoalRepository.findByUserAndGoal(user, goal).orElseThrow(() -> new UserGoalException(ErrorStatus.NOT_FOUND_USERGOAL));
    }

    @Override
    public List<UserGoal> getUserGoalListByGoal(Goal goal) {
        return userGoalRepository.findAllByGoal(goal);
    }

    @Override
    public VerificationType checkVerificationType(UserGoal userGoal) {
        return userGoal.getGoal().getVerificationType();
    }

    @Override
    public List<UserGoal> getUserGoalInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return userGoalRepository.findAllByUpdatedAtBetween(startDate, endDate);
    }

    @Override
    public UserGoal getByGoalIdAndUserId(Long goalId, Long userId) {
        return userGoalRepository.findByGoalIdAndUserId(goalId, userId);
    }

    @Override
    public boolean existUserGoal(Long goalId, Long userId) {
        return userGoalRepository.existsUserGoalByGoalIdAndUserId(goalId, userId);
    }

    @Override
    public Integer getUserGoalCount(Long userId) {
        return Math.toIntExact(userGoalRepository.countByUserId(userId));
    }

    @Override
    public List<UserWithGoalCountDTO> getUserByChallengesAndUserId(Long userId) {
        return userGoalRepository.getUserByChallengesAndUserId(userId,
                List.of(GoalType.CHALLENGE_TIME, GoalType.CHALLENGE_PHOTO));
    }

    @Override
    public List<UserWithGoalCountDTO> getUserGoalCntByUserIds(List<Long> userIds) {
        return userGoalRepository.getUserGoalCntByUserIds(userIds);
    }
}
