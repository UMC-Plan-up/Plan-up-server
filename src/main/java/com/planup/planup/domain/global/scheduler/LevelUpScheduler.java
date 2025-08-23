package com.planup.planup.domain.global.scheduler;

import com.planup.planup.domain.goal.service.UserLevelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LevelUpScheduler {

    private final UserLevelService userLevelService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void checkLevelUp() {
        userLevelService.checkAndUpgradeAllUsers();
        log.info("전체 유저 레벨업 체크 완료");
    }
}