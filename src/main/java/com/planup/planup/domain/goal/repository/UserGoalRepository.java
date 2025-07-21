package com.planup.planup.domain.goal.repository;

import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {

}
