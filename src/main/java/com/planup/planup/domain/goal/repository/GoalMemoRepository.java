package com.planup.planup.domain.goal.repository;

import com.planup.planup.domain.goal.entity.GoalMemo;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface GoalMemoRepository extends JpaRepository<GoalMemo, Long> {
    Optional<GoalMemo> findByUserGoalAndMemoDate(UserGoal userGoal, LocalDate memoDate);

    @Query("DELETE FROM GoalMemo gm WHERE gm.userGoal.goal.id = :goalId")
    @Modifying
    void deleteByGoalId(Long goalId);
}
