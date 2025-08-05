package com.planup.planup.domain.verification.repository;

import com.planup.planup.domain.verification.entity.PhotoVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoVerificationRepository extends JpaRepository<PhotoVerification, Long> {
}
