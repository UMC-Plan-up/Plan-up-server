package com.planup.planup.domain.bedge.repository;

import com.planup.planup.domain.bedge.entity.SpecificGoalDays;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SpecificGoalDaysRepository extends JpaRepository<SpecificGoalDays, Long> {

    List<SpecificGoalDays> findAllByUserIdAAndLastUpdate(Long userId, LocalDate date);
}
