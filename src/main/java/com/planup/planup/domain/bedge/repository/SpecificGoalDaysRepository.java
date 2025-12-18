package com.planup.planup.domain.bedge.repository;

import com.planup.planup.domain.bedge.entity.SpecificGoalDays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SpecificGoalDaysRepository extends JpaRepository<SpecificGoalDays, Long> {

    List<SpecificGoalDays> findAllByUserStat_IdAndLastUpdate(Long userId, LocalDate date);
}
