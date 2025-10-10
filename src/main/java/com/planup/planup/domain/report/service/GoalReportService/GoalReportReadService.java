package com.planup.planup.domain.report.service.GoalReportService;

import com.planup.planup.domain.report.dto.GoalReportResponseDTO;
import com.planup.planup.domain.report.entity.GoalReport;

import java.time.LocalDateTime;
import java.util.List;

public interface GoalReportReadService {
    //리포트를 조회한다.
    GoalReport getGoalReportOrThrow(Long id);

    //goalReport와 해당 리포트의 댓글을 조회하여 DTO로 반환
    GoalReportResponseDTO.GoalReportResponse findDTOById(Long id, Long userId);

    //가장 최근의 리포트 2개를 반환한다.
    List<GoalReport> findTop2RecentByGoalId(Long id);

    //유저의 특정 기간 리포트를 조회한다.
    List<GoalReport> getListByUserIdOneDay(Long userId, LocalDateTime start, LocalDateTime end);

    List<GoalReport> getGoalReportsByUserAndPeriod(Long userId, LocalDateTime start, LocalDateTime end);
}
