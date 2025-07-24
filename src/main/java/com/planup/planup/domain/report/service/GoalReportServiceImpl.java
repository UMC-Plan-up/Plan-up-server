package com.planup.planup.domain.report.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.ReportException;
import com.planup.planup.domain.report.converter.GoalReportConverter;
import com.planup.planup.domain.report.dto.GoalReportResponseDTO;
import com.planup.planup.domain.report.entity.GoalReport;
import com.planup.planup.domain.report.repository.GoalReportRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class GoalReportServiceImpl implements GoalReportService {

    private final GoalReportRepository goalReportRepository;
    @Override
    public GoalReportResponseDTO.GoalReportResponse findByGoalId(Long id) {
        GoalReport goalReport = goalReportRepository.findById(id).orElseThrow(() -> new ReportException(ErrorStatus.NOT_FOUND_GOAL_REPORT));

        return GoalReportConverter.toResponse(goalReport);
    }
}
