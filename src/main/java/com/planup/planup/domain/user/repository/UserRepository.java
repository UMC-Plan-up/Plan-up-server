package com.planup.planup.domain.user.repository;

import com.planup.planup.domain.user.dto.UserProfileDTO;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.enums.UserActivate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndUserActivate(String email, UserActivate userActivate);

    Optional<User> findByIdAndUserActivate(Long userId, UserActivate userActivate);

    boolean existsByEmailAndUserActivate(String email, UserActivate userActivate);

    @Query("""
    select new com.planup.planup.domain.user.dto.UserProfileDTO(
        u.id,
        u.name,
        u.profileImg
    )
    from User u
    where u.id = :userId
    """)
    Optional<UserProfileDTO> findUserProfileById(@Param("userId") Long userId);

    @Query("""
    select new com.planup.planup.domain.user.dto.UserProfileDTO(
    u.id,
    u.name,
    u.profileImg
    )
    from User u
    where u.id in :userIds
""")
    List<UserProfileDTO> findUserProfileByIds(@Param("userId") List<Long> userIds);
}
