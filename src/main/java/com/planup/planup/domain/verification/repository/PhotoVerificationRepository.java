package com.planup.planup.domain.verification.repository;

import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhotoVerificationRepository extends JpaRepository<PhotoVerification, Long> {

    List<PhotoVerification> findAllByUserGoalAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
}
