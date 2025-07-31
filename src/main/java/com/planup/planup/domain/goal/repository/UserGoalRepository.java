package com.planup.planup.domain.goal.repository;

import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import com.planup.planup.domain.user.entity.User;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {
    //Query Service
    UserGoal findByGoalIdAndStatus(Long goalId, Status status);

    UserGoal findByGoalIdAndUserId(Long goalId, Long userId);

    List<UserGoal> findByUserId(Long userId);

    int countByGoalIdAndIsActiveTrue(Long id);

    //Command Service

    Optional<UserGoal> findByUserAndGoal(User user, Goal goal);

    List<UserGoal> findAllByGoal(Goal goal);
}
