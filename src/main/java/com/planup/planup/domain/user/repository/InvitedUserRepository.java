package com.planup.planup.domain.user.repository;

import com.planup.planup.domain.user.entity.InvitedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitedUserRepository extends JpaRepository<InvitedUser, Long> {
}
