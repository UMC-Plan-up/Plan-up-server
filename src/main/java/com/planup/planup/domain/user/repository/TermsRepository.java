package com.planup.planup.domain.user.repository;

import com.planup.planup.domain.user.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermsRepository extends JpaRepository<Terms, Long> {

    List<Terms> findAllByOrderByOrderAsc();
    List<Terms> findByIsRequiredTrue();
}
