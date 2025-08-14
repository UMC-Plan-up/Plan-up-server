package com.planup.planup.domain.verification.repository;

import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.verification.entity.TimerVerification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimerVerificationRepository extends JpaRepository<TimerVerification, Long> {

    //Query Service

    //오늘 날짜와 기록된 날짜 비교 쿼리
    @Query("SELECT t FROM TimerVerification t WHERE t.userGoal.id = :userGoalId " +
            "AND DATE(t.createdAt) = CURRENT_DATE")
    List<TimerVerification> findTodayVerificationsByUserGoalId(@Param("userGoalId") Long userGoalId);

    List<TimerVerification> findByUserGoal_User_IdAndEndTimeIsNull(Long userId);

    List<TimerVerification> findAllByUserGoalAndCreatedAtBetweenOrderByCreatedAt(UserGoal userGoal, LocalDateTime start, LocalDateTime end);

    @Query("""
    SELECT tv FROM TimerVerification tv
    WHERE tv.userGoal.user = :user
      AND tv.createdAt BETWEEN :startDate AND :endDate
    ORDER BY tv.createdAt DESC
""")
    List<TimerVerification> findTop5ByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    List<TimerVerification> findAllByUserGoal(UserGoal userGoal);
}


