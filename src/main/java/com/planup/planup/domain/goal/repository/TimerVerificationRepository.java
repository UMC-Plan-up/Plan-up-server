package com.planup.planup.domain.goal.repository;

import com.planup.planup.domain.goal.entity.TimerVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimerVerificationRepository extends JpaRepository<TimerVerification, Long> {
}
