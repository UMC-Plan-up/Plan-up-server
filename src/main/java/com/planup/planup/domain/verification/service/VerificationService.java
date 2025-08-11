package com.planup.planup.domain.verification.service;

import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public interface VerificationService {
    @Transactional
    Map<LocalDate, Integer> calculateVerification(UserGoal userGoal, LocalDateTime startDate, LocalDateTime endDate);
}
