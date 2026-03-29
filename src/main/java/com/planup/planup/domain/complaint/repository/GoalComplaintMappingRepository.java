package com.planup.planup.domain.complaint.repository;

import com.planup.planup.domain.complaint.entity.GoalComplaintMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoalComplaintMappingRepository extends JpaRepository<GoalComplaintMapping, Long> {

    boolean existsByReporterIdAndGoalId(Long reporterId, Long goalId);

    // 신고자가 신고한 goalId 목록 (조회 필터링용)
    @Query("SELECT g.goal.id FROM GoalComplaintMapping g WHERE g.reporter.id = :reporterId")
    List<Long> findReportedGoalIdsByReporterId(@Param("reporterId") Long reporterId);
}
