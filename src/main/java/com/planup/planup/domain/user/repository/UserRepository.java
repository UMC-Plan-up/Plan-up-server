package com.planup.planup.domain.user.repository;

import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.enums.UserActivate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndUserActivate(String email, UserActivate userActivate);

    Optional<User> findByIdAndUserActivate(Long userId, UserActivate userActivate);

    boolean existsByEmailAndUserActivate(String email, UserActivate userActivate);
}
