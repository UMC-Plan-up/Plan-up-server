package com.planup.planup.domain.goal.repository;

import com.planup.planup.domain.goal.entity.TimeChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeChallengeRepository extends JpaRepository<TimeChallenge, Long> {
}
