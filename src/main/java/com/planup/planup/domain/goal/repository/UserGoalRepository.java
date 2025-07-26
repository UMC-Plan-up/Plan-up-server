package com.planup.planup.domain.goal.repository;

import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {
    //Query Service
    List<UserGoal> findByUserIdAndIsActiveTrue(Long userId);

    UserGoal findByGoalIdAndStatus(Long goalId, Status status);

    int countByGoalIdAndIsActiveTrue(Long goalId);

    @Query("SELECT ug FROM UserGoal ug " +
            "JOIN FETCH ug.goal g " +
            "LEFT JOIN FETCH ug.timerVerifications tv " +
            "LEFT JOIN FETCH ug.photoVerifications pv " +
            "WHERE ug.user.id = :userId AND ug.isActive = true")
    List<UserGoal> findByUserIdAndIsActiveTrueWithVerifications(Long userId);

    UserGoal findByGoalIdAndUserId(Long goalId, Long userId);

    //Command Service

}
