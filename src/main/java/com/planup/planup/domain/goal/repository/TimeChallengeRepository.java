package com.planup.planup.domain.goal.repository;

import com.planup.planup.domain.goal.entity.TimeChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

@Repository
public interface TimeChallengeRepository extends JpaRepository<TimeChallenge, Long> {
}
