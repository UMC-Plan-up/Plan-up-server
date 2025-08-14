package com.planup.planup.domain.user.repository;

import com.planup.planup.domain.user.entity.Adjective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdjectiveRepository extends JpaRepository<Adjective, Long> {
}
