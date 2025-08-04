package com.planup.planup.domain.user.verification.repository;

import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.verification.entity.PhotoVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhotoVerificationRepository extends JpaRepository<PhotoVerification, Long> {

    List<PhotoVerification> findAllByUserGoalAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
}
