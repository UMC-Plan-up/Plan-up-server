package com.planup.planup.domain.user.repository;

import com.planup.planup.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);
<<<<<<< HEAD
    
=======

>>>>>>> 4c1817c1fd3a4d5ba7f3a3742725f5d6d30b0e8f
    boolean existsByEmail(String email);
}
