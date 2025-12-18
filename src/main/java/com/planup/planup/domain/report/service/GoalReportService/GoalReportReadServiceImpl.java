package com.planup.planup.domain.report.service.GoalReportService;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.ReportException;
import com.planup.planup.domain.goal.convertor.CommentConverter;
import com.planup.planup.domain.goal.dto.CommentResponseDto;
import com.planup.planup.domain.goal.entity.Comment;
import com.planup.planup.domain.report.converter.GoalReportConverter;
import com.planup.planup.domain.report.dto.GoalReportResponseDTO;
import com.planup.planup.domain.report.entity.GoalReport;
import com.planup.planup.domain.report.repository.GoalReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalReportReadServiceImpl implements GoalReportReadService {

    private final GoalReportRepository goalReportRepository;

    //리포트를 조회한다.
    @Override
    public GoalReport getGoalReportOrThrow(Long id) {
        return goalReportRepository.findById(id)
                .orElseThrow(() -> new ReportException(ErrorStatus.NOT_FOUND_GOAL_REPORT));
    }

    //goalReport와 해당 리포트의 댓글을 조회하여 DTO로 반환
    @Override
    public GoalReportResponseDTO.GoalReportResponse findDTOById(Long id, Long userId) {
        GoalReport goalReport = getGoalReportOrThrow(id);
        List<CommentResponseDto.CommentDto> commentDtoList = getCommentResponseDtoListByGoalReport(goalReport);
        return GoalReportConverter.toGoalReportResponse(goalReport, commentDtoList);
    }

    //리포트에 연결된 코멘트 관련 조회 및 DTO 변환
    private List<CommentResponseDto.CommentDto> getCommentResponseDtoListByGoalReport(GoalReport goalReport) {
        List<Comment> commentList = goalReport.getCommentList();
        return CommentConverter.toResponseDtoList(commentList, goalReport.getUserId());
    }

    //가장 최근의 리포트 2개를 반환한다.
    @Override
    public List<GoalReport> findTop2RecentByGoalId(Long id) {
        return goalReportRepository.findTop2ByGoalIdOrderByCreatedAt(id);
    }

    //유저의 특정 기간 리포트를 조회한다.
    @Override
    public List<GoalReport> getListByUserIdOneDay(Long userId, LocalDateTime start, LocalDateTime end) {
        return goalReportRepository.findAllByUserIdAndCreatedAtBetween(userId, start, end);
    }

    @Override
    public List<GoalReport> getGoalReportsByUserAndPeriod(Long userId, LocalDateTime start, LocalDateTime end) {
        return goalReportRepository.findAllByUserIdAndCreatedAtBetween(userId, start, end);
    }

}
