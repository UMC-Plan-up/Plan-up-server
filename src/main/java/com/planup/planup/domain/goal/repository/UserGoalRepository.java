package com.planup.planup.domain.goal.repository;

import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {
    //Query Service
    UserGoal findByGoalIdAndStatus(Long goalId, Status status);

    UserGoal findByGoalIdAndUserId(Long goalId, Long userId);

    List<UserGoal> findByUserId(Long userId);

    int countByGoalIdAndIsActiveTrue(Long id);

    @Query("SELECT ug FROM UserGoal ug " +
            "JOIN ug.goal g " +
            "JOIN Friend f ON (f.user.id = :userId AND f.friend.id = ug.user.id AND f.status = 'ACCEPTED') " +
            "OR (f.friend.id = :userId AND f.user.id = ug.user.id AND f.status = 'ACCEPTED') " +
            "WHERE ug.isPublic = true " +
            "AND g.goalCategory = :goalCategory " +
            "AND g.goalType = 'FRIEND'")
    List<UserGoal> findFriendGoalsByCategory(@Param("userId") Long userId,
                                             @Param("goalCategory") GoalCategory goalCategory);

    @Query("SELECT ug FROM UserGoal ug " +
            "JOIN ug.goal g " +
            "WHERE ug.isPublic = true " +
            "AND g.goalCategory = :goalCategory " +
            "AND g.goalType = 'COMMUNITY'")
    List<UserGoal> findCommunityGoalsByCategory(@Param("goalCategory") GoalCategory goalCategory);

    int countByGoalId(Long goalId);
    //Command Service

}
