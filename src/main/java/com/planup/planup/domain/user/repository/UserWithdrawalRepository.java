package com.planup.planup.domain.user.repository;

import com.planup.planup.domain.user.entity.UserWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface UserWithdrawalRepository extends JpaRepository<UserWithdrawal, Long> {
    @Modifying
    @Query("DELETE FROM UserWithdrawal w WHERE w.createdAt < :date")
    void deleteByCreatedAtBefore(@Param("date") LocalDateTime date);
}