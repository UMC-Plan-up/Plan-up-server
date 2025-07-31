package com.planup.planup.domain.user.verification.repository;

import com.planup.planup.domain.user.verification.entity.PhotoVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoVerificationRepository extends JpaRepository<PhotoVerification, Long> {
}
