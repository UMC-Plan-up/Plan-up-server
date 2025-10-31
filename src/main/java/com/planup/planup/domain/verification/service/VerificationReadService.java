package com.planup.planup.domain.verification.service;

import com.planup.planup.domain.report.entity.DailyRecord;
import com.planup.planup.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface VerificationReadService {
    List<DailyRecord> getDailyRecordForWeeklyReport(User user, LocalDateTime start, LocalDateTime end);
}
