package com.planup.planup.domain.global.scheduler;

import com.planup.planup.domain.goal.service.GoalLifeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoalExpireScheduler {

    private final GoalLifeService goalLifeService;

    @Scheduled(cron = "0 0 0 * * MON", zone = "Asia/Seoul")
    public void checkExpireGoal() {
        goalLifeService.disableExpiredGoals(new Date());
    }
}
