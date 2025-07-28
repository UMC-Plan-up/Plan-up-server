package com.planup.planup.domain.user.repository;

import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.entity.UserStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStatRepository extends JpaRepository<UserStat, Long> {

    List<UserStat> findAllByMarkedChange(boolean markedChange);

    UserStat findByUser(User user);
}
