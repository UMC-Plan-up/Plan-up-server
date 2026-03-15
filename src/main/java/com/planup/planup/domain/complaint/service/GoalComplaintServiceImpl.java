package com.planup.planup.domain.complaint.service;

import com.planup.planup.domain.complaint.dto.ComplaintRequestDTO;
import com.planup.planup.domain.complaint.entity.GoalComplaintMapping;
import com.planup.planup.domain.complaint.repository.GoalComplaintMappingRepository;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.goal.repository.GoalRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GoalComplaintServiceImpl implements GoalComplaintService {

    private final GoalComplaintMappingRepository goalComplaintMappingRepository;
    private final GoalRepository goalRepository;
    private final UserQueryService userQueryService;

    @Override
    @Transactional
    public void reportGoal(Long reporterId, Long goalId, ComplaintRequestDTO request) {
        if (goalComplaintMappingRepository.existsByReporterIdAndGoalId(reporterId, goalId)) {
            throw new IllegalStateException("이미 신고한 커뮤니티입니다.");
        }

        User reporter = userQueryService.getUserByUserId(reporterId);
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 커뮤니티입니다."));

        GoalComplaintMapping complaint = GoalComplaintMapping.builder()
                .reporter(reporter)
                .goal(goal)
                .reason(request.getReason())
                .build();

        goalComplaintMappingRepository.save(complaint);

        goal.incrementComplaintCount();

        // 1회: 신고자 기준 쿼리 필터링으로만 처리 (별도 플래그 불필요)
        // 3회 누적: 즉시 14일 비활성화
        if (goal.getComplaintCount() >= 5) {
            goal.deleteGoal();
            log.info("커뮤니티 영구 삭제 조치: goalId={}, complaintCount={}", goalId, goal.getComplaintCount());
        } else if (goal.getComplaintCount() >= 3) {
            goal.suspendGoal();
            log.info("커뮤니티 14일 비활성화 조치: goalId={}, complaintCount={}", goalId, goal.getComplaintCount());
        }
    }

    @Override
    public List<Long> getReportedGoalIds(Long reporterId) {
        return goalComplaintMappingRepository.findReportedGoalIdsByReporterId(reporterId);
    }
}
