package com.planup.planup.domain.global.scheduler;

import com.planup.planup.domain.user.entity.UserStat;
import com.planup.planup.domain.user.repository.UserStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatResetScheduler {

    private final UserStatRepository userStatRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void resetUserDailyStats() {
        List<UserStat> statsToReset = userStatRepository.findAllByMarkedChange(true);

        for (UserStat stat : statsToReset) {
            stat.resetDailyStats();
        }

        userStatRepository.saveAll(statsToReset);
        log.info("Daily reset 완료");
    }

    @Scheduled(cron = "0 0 0 * * MON", zone = "Asia/Seoul")
    public void resetUserStatPerWeek() {

    }

}
