package com.planup.planup.domain.goal.repository;

import com.planup.planup.domain.goal.dto.UserWithGoalCountDTO;
import com.planup.planup.domain.goal.entity.Enum.GoalCategory;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import com.planup.planup.domain.goal.entity.Enum.Status;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;

@Repository
public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {
    //Query Service
    UserGoal findByGoalIdAndStatus(Long goalId, Status status);
    List<UserGoal> findByGoalId(Long goalId);

    UserGoal findByGoalIdAndUserId(Long goalId, Long userId);

    List<UserGoal> findByUserId(Long userId);

    @Query("SELECT ug FROM UserGoal ug " +
            "JOIN ug.goal g " +
            "WHERE ug.isPublic = true " +
            "AND g.goalCategory = :goalCategory " +
            "AND g.goalType = 'FRIEND' " +
            "AND EXISTS (" +
            "    SELECT 1 FROM Friend f " +
            "    WHERE f.status = 'ACCEPTED' " +
            "    AND ((f.user.id = :userId AND f.friend.id = ug.user.id) " +
            "         OR (f.friend.id = :userId AND f.user.id = ug.user.id))" +
            ")")
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

    Optional<UserGoal> findByUserAndGoal(User user, Goal goal);

    List<UserGoal> findAllByGoal(Goal goal);

    List<UserGoal> findAllByUserAndGoal(User user, Goal goal);

    List<UserGoal> findAllByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<UserGoal> findByUserIdAndIsPublicTrue(Long friendsId);

    List<UserGoal> findByGoalIdOrderByVerificationCountDesc(Long goalId);

    boolean existsUserGoalByGoalIdAndUserId(Long goalId, Long userId);

    int countByUserIdAndIsActiveTrue(Long id);

    List<UserGoal> findByUserIdAndIsActiveTrueOrderByCreatedAtAsc(Long id);

    @Query("SELECT ug FROM UserGoal ug " +
            "WHERE ug.user = :user " +
            "AND ug.isActive = true " +
            "AND (ug.goal.endDate IS NULL OR ug.goal.endDate >= :targetDate)")
    List<UserGoal> findActiveUserGoalsByUser(@Param("user") User user, @Param("targetDate") LocalDate targetDate);

    @Query("""
        select new com.planup.planup.domain.goal.dto.UserWithGoalCountDTO(
            ug.user, count(ug)
        )
        from UserGoal ug
        where ug.user.id in :userIds
        and ug.goal.isActive = true
        group by ug.user.id
    """)
    List<UserWithGoalCountDTO> getUserGoalCntByUserIds(@Param("userIds") List<Long> userIds);

    @Query("select count(ug) " +
                  "from UserGoal ug " +
                  "where ug.user.id = :userId")
    Integer countByUserId(@Param("userId") Long userId);

    @Query("""
select new com.planup.planup.domain.goal.dto.UserWithGoalCountDTO(
    ug.user, count(ug)
)
from UserGoal ug
join ug.goal g
where ug.user.id = :userId
  and g.goalType in :challengeTypes
group by ug.user
""")
    List<UserWithGoalCountDTO> getUserByChallengesAndUserId(@Param("userId") Long userId,
                                                            @Param("challengeTypes") List<GoalType> challengeTypes);


}


