package com.planup.planup.domain.goal.repository;

import com.planup.planup.domain.goal.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
}
