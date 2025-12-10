package com.planup.planup.domain.user.repository;

import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.bedge.entity.UserStat;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatRepository extends JpaRepository<UserStat, Long> {

    List<UserStat> findAllByUpdatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<UserStat> findByUser(User user);

    Optional<UserStat> findByUser_Id(Long userId);

    @Query("""
    select distinct us
    from UserStat us
    left join fetch us.recordAllGoal7Days sg
    where us.user.id = :userId
      and (
        sg is null
        or sg.lastUpdate between :startDate and :endDate
      )
    """)
    Optional<UserStat> findWithSpecificGoalDaysByUserIdAndLastUpdateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
