package com.planup.planup.domain.goal.repository;

import com.planup.planup.domain.goal.entity.PhotoChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoChallengeRepository extends JpaRepository<PhotoChallenge, Long> {
}
