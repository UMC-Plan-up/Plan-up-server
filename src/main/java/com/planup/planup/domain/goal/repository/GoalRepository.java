package com.planup.planup.domain.goal.repository;

import com.planup.planup.domain.goal.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByEndDateBeforeAndIsActiveTrue(Date now);

    // 14일 정지 기간이 만료된 커뮤니티 조회 (영구 삭제는 sanctionEndAt=null이므로 제외됨)
    @Query("SELECT g FROM Goal g WHERE g.isActive = false AND g.sanctionEndAt IS NOT NULL AND g.sanctionEndAt <= :now")
    List<Goal> findExpiredSuspendedGoals(@Param("now") LocalDateTime now);
}
