package com.planup.planup.domain.verification.repository;

import com.planup.planup.domain.goal.entity.mapping.UserGoal;
import com.planup.planup.domain.verification.entity.PhotoVerification;
import com.planup.planup.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhotoVerificationRepository extends JpaRepository<PhotoVerification, Long> {

    @Query("SELECT COUNT(p) > 0 FROM PhotoVerification p " +
           "WHERE p.userGoal.user.id = :userId " +
           "AND DATE(p.createdAt) = CURRENT_DATE")
    boolean existsTodayPhotoVerificationByUserId(@Param("userId") Long userId);

    List<PhotoVerification> findAllByUserGoalAndCreatedAtBetweenOrderByCreatedAt(UserGoal usergoal, LocalDateTime start, LocalDateTime end);

    @Query("""
    SELECT pv FROM PhotoVerification pv
    WHERE pv.userGoal.user = :user
      AND pv.createdAt BETWEEN :startDate AND :endDate
    ORDER BY pv.createdAt DESC
""")
    List<PhotoVerification> findTop5ByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM PhotoVerification p " +
            "WHERE p.userGoal.id = :userGoalId " +
            "AND DATE(p.createdAt) = :date")
    boolean existsByUserGoalAndDate(@Param("userGoalId") Long userGoalId,
                                    @Param("date") LocalDate date);

    List<PhotoVerification> findAllByUserGoal(UserGoal userGoal);

    boolean existsByUserGoal_User_IdAndCreatedAtAfter(Long userId, LocalDateTime localDateTime);
}
