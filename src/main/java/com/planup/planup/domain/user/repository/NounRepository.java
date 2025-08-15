package com.planup.planup.domain.user.repository;

import com.planup.planup.domain.user.entity.Noun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NounRepository extends JpaRepository<Noun, Long> {
}
