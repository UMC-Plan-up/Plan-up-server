package com.planup.planup.domain.goalphoto.repository;

import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.goalphoto.entity.GoalPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GoalPhotoRepository extends JpaRepository<GoalPhoto, Long> {

    List<GoalPhoto> findAllByUserGoalAndDateOrderByCreatedAtDesc(UserGoal userGoal, LocalDate date);

    void deleteAllByUserGoalAndDate(UserGoal userGoal, LocalDate date);
}
