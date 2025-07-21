package com.planup.planup.domain.bedge.repository;

import com.planup.planup.domain.bedge.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Badge, Long> {
}
