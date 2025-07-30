package com.planup.planup.domain.user.repository;

import com.planup.planup.domain.user.entity.UserTerms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTermsRepository extends JpaRepository<UserTerms, Long> {
}
