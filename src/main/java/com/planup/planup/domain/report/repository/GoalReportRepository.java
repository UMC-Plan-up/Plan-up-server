package com.planup.planup.domain.report.repository;

import com.planup.planup.domain.report.entity.GoalReport;
import io.swagger.v3.oas.annotations.Operation;
import org.aspectj.apache.bcel.classfile.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoalReportRepository extends JpaRepository<GoalReport, Long> {

    Optional<GoalReport> findById(Long id);

    List<GoalReport> findTop2ByGoalIdOrderByCreatedAt(Long id);

    List<GoalReport> findAllByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
