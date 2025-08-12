package com.planup.planup.domain.goal.repository;

import com.planup.planup.domain.goal.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByEndDateBeforeAndIsActiveTrue(Date now);
}
