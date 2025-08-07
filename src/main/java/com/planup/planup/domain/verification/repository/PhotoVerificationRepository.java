package com.planup.planup.domain.verification.repository;

import com.planup.planup.domain.verification.entity.PhotoVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoVerificationRepository extends JpaRepository<PhotoVerification, Long> {

    @Query("SELECT COUNT(p) > 0 FROM PhotoVerification p " +
           "WHERE p.userGoal.user.id = :userId " +
           "AND DATE(p.createdAt) = CURRENT_DATE")
    boolean existsTodayPhotoVerificationByUserId(@Param("userId") Long userId);
}
