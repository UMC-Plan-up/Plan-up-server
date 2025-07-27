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
    List<UserGoal> findByUserIdAndActiveTrue(Long userId);

    UserGoal findByGoalIdAndStatus(Long goalId, Status status);

    int countByGoalIdAndActiveTrue(Long goalId);

    UserGoal findByGoalIdAndUserId(Long goalId, Long userId);

    List<UserGoal> findByUserId(Long userId);

    //Command Service

}
