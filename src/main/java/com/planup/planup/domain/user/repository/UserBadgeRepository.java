package com.planup.planup.domain.user.repository;

import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUserAndCreatedAtBetween(User user, LocalDateTime from, LocalDateTime to);

    List<UserBadge> findTop5ByUserOrderByCreatedAtDesc(User user);

    List<UserBadge> findByUserOrderByCreatedAtDesc(User user);
}
