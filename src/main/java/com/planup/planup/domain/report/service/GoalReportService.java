package com.planup.planup.domain.report.service;

import com.planup.planup.domain.report.dto.GoalReportResponseDTO;
import com.planup.planup.domain.report.entity.GoalReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public interface GoalReportService {

    GoalReportResponseDTO.GoalReportResponse findByGoalId(Long id);
}
