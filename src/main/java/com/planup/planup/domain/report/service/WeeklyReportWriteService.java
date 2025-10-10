package com.planup.planup.domain.report.service;

import com.planup.planup.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface WeeklyReportWriteService {
    @Transactional
    void createWeeklyReport(User user, LocalDateTime startDate);
}
