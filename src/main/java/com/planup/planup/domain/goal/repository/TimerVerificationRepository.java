package com.planup.planup.domain.goal.repository;

import com.planup.planup.domain.goal.entity.TimerVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimerVerificationRepository extends JpaRepository<TimerVerification, Long> {

    //Query Service

    //오늘 날짜와 기록된 날짜 비교 쿼리
    @Query("SELECT t FROM TimerVerification t WHERE t.userGoal.id = :userGoalId " +
            "AND DATE(t.createdAt) = CURRENT_DATE")
    List<TimerVerification> findTodayVerificationsByUserGoalId(@Param("userGoalId") Long userGoalId);}
