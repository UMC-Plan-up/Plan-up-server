package com.planup.planup.domain.verification.repository;

import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhotoVerificationRepository extends JpaRepository<PhotoVerification, Long> {

    List<PhotoVerification> findAllByUserGoalAndCreatedAtBetweenOrderByCreatedAt(UserGoal usergoal, LocalDateTime start, LocalDateTime end);

    @Query("""
    SELECT pv FROM PhotoVerification pv
    WHERE pv.userGoal.user = :user
      AND pv.createdAt BETWEEN :startDate AND :endDate
    ORDER BY pv.createdAt DESC
""")
    List<PhotoVerification> findTop5ByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
