package com.planup.planup.domain.goal.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.goal.entity.Enum.VerificationType;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.report.entity.DailyAchievementRate;
import com.planup.planup.domain.report.service.GoalReportServiceImpl;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserLevel;
import com.planup.planup.domain.user.repository.UserRepository;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import com.planup.planup.domain.verification.repository.TimerVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLevelService {
    private final UserRepository userRepository;
    private final UserGoalRepository userGoalRepository;
    private final GoalReportServiceImpl goalReportService;
    private final TimerVerificationRepository timerVerificationRepository;
    private final PhotoVerificationRepository photoVerificationRepository;


    @Transactional
    public boolean checkAndUpgradeLevel(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));

        if (!couldPossiblyLevelUp(user)) {
            return false;
        }

        UserLevel currentLevel = user.getUserLevel();
        List<UserGoal> activeGoals = userGoalRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtAsc(user.getId());

        // LEVEL_1 → LEVEL_2: 첫 번째 목표를 7일 내에 50% 이상 달성
        if (currentLevel == UserLevel.LEVEL_1) {
            if (activeGoals.isEmpty()) return false;

            UserGoal firstGoal = activeGoals.get(0);
            int weeklyRate = calculateWeeklyAchievementRate(firstGoal);

            if (weeklyRate >= 50) {
                user.setUserLevel(UserLevel.LEVEL_2);
                userRepository.save(user);
                return true;
            }
            return false;
        }

        if (currentLevel == UserLevel.LEVEL_2) {
            if (activeGoals.size() < 2) return false;

            UserGoal newestGoal = activeGoals.stream()
                    .max(Comparator.comparing(UserGoal::getCreatedAt))
                    .orElse(null);

            if (newestGoal != null && hasConsecutiveDailyVerifications(newestGoal, 7)) {
                user.setUserLevel(UserLevel.LEVEL_3);
                userRepository.save(user);
                return true;
            }
            return false;
        }

        // LEVEL_3 → LEVEL_4: 2개의 활성 목표를 모두 7일 내에 50% 이상 달성
        if (currentLevel == UserLevel.LEVEL_3) {
            if (activeGoals.size() < 2) return false;

            long achievedGoals = activeGoals.stream()
                    .filter(userGoal -> calculateWeeklyAchievementRate(userGoal) >= 50)
                    .count();

            if (achievedGoals >= 2) {
                user.setUserLevel(UserLevel.LEVEL_4);
                userRepository.save(user);
                return true;
            }
            return false;
        }

        // LEVEL_4 → LEVEL_5: 7일간 2개 이상의 활성화 목표 달성률을 60% 이상 + 새목표 추가 필수, 활성화 목표수 3
        if (currentLevel == UserLevel.LEVEL_4) {
            if (activeGoals.size() < 3) return false;

            long achievedGoals = activeGoals.stream()
                    .mapToInt(this::calculateWeeklyAchievementRate)
                    .filter(rate -> rate >= 60)
                    .count();

            if (achievedGoals >= 2) {
                user.setUserLevel(UserLevel.LEVEL_5);
                userRepository.save(user);
                return true;
            }
            return false;
        }

        // LEVEL_5 → LEVEL_6: 7일간 전체 활성화 목표 달성률 50% 이상
        if (currentLevel == UserLevel.LEVEL_5) {
            if (activeGoals.size() < 3) return false;

            boolean allGoalsAchieved = activeGoals.stream()
                    .allMatch(goal -> calculateWeeklyAchievementRate(goal) >= 50);

            if (allGoalsAchieved) {
                user.setUserLevel(UserLevel.LEVEL_6);
                userRepository.save(user);
                return true;
            }
            return false;
        }

        // LEVEL_6 → LEVEL_7: 14일간 2개 이상의 활성화 목표 달성률을 50% 이상
        if (currentLevel == UserLevel.LEVEL_6) {
            if (activeGoals.size() < 3) return false;

            long achievedGoals = activeGoals.stream()
                    .mapToInt(this::calculateTwoWeekAchievementRate)
                    .filter(rate -> rate >= 50)
                    .count();

            if (achievedGoals >= 2) {
                user.setUserLevel(UserLevel.LEVEL_7);
                userRepository.save(user);
                return true;
            }
            return false;
        }

        // LEVEL_7 → LEVEL_8: 14일간 전체 활성화 목표 달성률을 50% 이상
        if (currentLevel == UserLevel.LEVEL_7) {
            if (activeGoals.size() < 3) return false;

            boolean allGoalsAchieved = activeGoals.stream()
                    .allMatch(goal -> calculateTwoWeekAchievementRate(goal) >= 50);


            if (allGoalsAchieved) {
                user.setUserLevel(UserLevel.LEVEL_8);
                userRepository.save(user);
                return true;
            }
            return false;
        }

        // LEVEL_8 → LEVEL_9: 14일간  3개 이상의 활성화 목표 달성률을 50% 이상
        if (currentLevel == UserLevel.LEVEL_8) {
            if (activeGoals.size() < 4) return false;

            long achievedGoals = activeGoals.stream()
                    .mapToInt(this::calculateTwoWeekAchievementRate)
                    .filter(rate -> rate >= 50)
                    .count();

            if (achievedGoals >= 3) {
                user.setUserLevel(UserLevel.LEVEL_9);
                userRepository.save(user);
                return true;
            }
            return false;
        }

        // LEVEL_9 → LEVEL_10: 21일간 3개 이상의 목표 달성률 50% 이상
        if (currentLevel == UserLevel.LEVEL_9) {
            if (activeGoals.size() < 3) return false;

            long achievedGoals = activeGoals.stream()
                    .mapToInt(this::calculateThreeWeekAchievementRate)
                    .filter(rate -> rate >= 50)
                    .count();

            if (achievedGoals >= 3) {
                user.setUserLevel(UserLevel.LEVEL_10);
                userRepository.save(user);
                return true;
            }
            return false;
        }

        // LEVEL_10 → LEVEL_MAX: 유료 결제 시에만 가능 (별도 메서드에서 처리)

        return false;
    }

    private boolean couldPossiblyLevelUp(User user) {
        if (user.getUserLevel() == UserLevel.LEVEL_MAX) {
            return false;
        }

        int activeGoalCount = userGoalRepository.countByUserIdAndIsActiveTrue(user.getId());
        if (activeGoalCount == 0) {
            return false;
        }

        if (!hasRecentVerifications(user.getId(), 7)) {
            return false;
        }

        return true;
    }

    private int calculateWeeklyAchievementRate(UserGoal userGoal) {
        LocalDateTime createdAt = userGoal.getCreatedAt().with(LocalTime.MIN);
        LocalDateTime now = LocalDateTime.now().with(LocalTime.MIN);

        int period = (int) ((java.time.Duration.between(createdAt, now).toDays()) / 7);
        LocalDateTime periodStart = createdAt.plusDays(period * 7);

        DailyAchievementRate rate = goalReportService.calculateDailyAchievementRate(
                userGoal, userGoal.getGoal(), periodStart
        );
        return rate.getTotal();
    }

    private int calculateTwoWeekAchievementRate(UserGoal userGoal) {
        LocalDateTime now = LocalDateTime.now().with(LocalTime.MIN);

        LocalDateTime week1Start = now.minusDays(7);
        LocalDateTime week2Start = now.minusDays(14);

        int week1Rate = goalReportService.calculateDailyAchievementRate(userGoal, userGoal.getGoal(), week1Start).getTotal();
        int week2Rate = goalReportService.calculateDailyAchievementRate(userGoal, userGoal.getGoal(), week2Start).getTotal();

        return (week1Rate + week2Rate) / 2;
    }

    private int calculateThreeWeekAchievementRate(UserGoal userGoal) {
        LocalDateTime now = LocalDateTime.now().with(LocalTime.MIN);

        LocalDateTime week1Start = now.minusDays(7);
        LocalDateTime week2Start = now.minusDays(14);
        LocalDateTime week3Start = now.minusDays(21);

        int week1Rate = goalReportService.calculateDailyAchievementRate(userGoal, userGoal.getGoal(), week1Start).getTotal();
        int week2Rate = goalReportService.calculateDailyAchievementRate(userGoal, userGoal.getGoal(), week2Start).getTotal();
        int week3Rate = goalReportService.calculateDailyAchievementRate(userGoal, userGoal.getGoal(), week3Start).getTotal();

        return (week1Rate + week2Rate + week3Rate) / 3;
    }

    //매일 인증 했는가
    private boolean hasConsecutiveDailyVerifications(UserGoal userGoal, int days) {
        LocalDate today = LocalDate.now();
        VerificationType type = userGoal.getGoal().getVerificationType();

        for (int i = 0; i < days; i++) {
            LocalDate checkDate = today.minusDays(i);
            boolean hasVerification = false;

            if (type == VerificationType.TIMER) {
                hasVerification = timerVerificationRepository
                        .existsByUserGoalAndDate(userGoal.getId(), checkDate);
            } else if (type == VerificationType.PHOTO) {
                hasVerification = photoVerificationRepository
                        .existsByUserGoalAndDate(userGoal.getId(), checkDate);
            }

            if (!hasVerification) {
                return false;
            }
        }
        return true;
    }

    private boolean hasRecentVerifications(Long userId, int days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);

        boolean hasTimer = timerVerificationRepository
                .existsByUserGoal_User_IdAndCreatedAtAfter(userId, cutoffDate.atStartOfDay());
        boolean hasPhoto = photoVerificationRepository
                .existsByUserGoal_User_IdAndCreatedAtAfter(userId, cutoffDate.atStartOfDay());

        return hasTimer || hasPhoto;
    }

    @Transactional
    public void upgradeToMaxLevel(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.NOT_FOUND_USER));

        user.setUserLevel(UserLevel.LEVEL_MAX);
        userRepository.save(user);

    }

    @Transactional
    public void checkAndUpgradeAllUsers() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            checkAndUpgradeLevel(user.getId());
        }
    }
}

