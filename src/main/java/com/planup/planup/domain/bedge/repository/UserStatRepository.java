package com.planup.planup.domain.bedge.repository;

import com.planup.planup.domain.bedge.entity.UserStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatRepository extends JpaRepository<UserStat, Long> {

    Optional<UserStat> findUserStatByUser_Id(Long userId);
}
