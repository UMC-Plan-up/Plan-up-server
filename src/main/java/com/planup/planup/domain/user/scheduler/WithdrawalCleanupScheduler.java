package com.planup.planup.domain.user.scheduler;

import com.planup.planup.domain.user.repository.UserWithdrawalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Transactional
public class WithdrawalCleanupScheduler {

    private final UserWithdrawalRepository userWithdrawalRepository;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldWithdrawals() {
        LocalDateTime nineDaysAgo = LocalDateTime.now().minusDays(90);
        userWithdrawalRepository.deleteByCreatedAtBefore(nineDaysAgo);
    }
}
