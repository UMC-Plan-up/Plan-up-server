package com.planup.planup.domain.global.scheduler;

import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoalSuspensionRestoreScheduler {

    private final GoalRepository goalRepository;

    // 매일 자정 실행 — 14일 정지 기간이 만료된 커뮤니티 자동 복구
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void restoreExpiredSuspendedGoals() {
        List<Goal> expiredGoals = goalRepository.findExpiredSuspendedGoals(LocalDateTime.now());

        for (Goal goal : expiredGoals) {
            goal.restore();
            log.info("커뮤니티 정지 해제: goalId={}, sanctionEndAt={}", goal.getId(), goal.getSanctionEndAt());
        }
    }
}
