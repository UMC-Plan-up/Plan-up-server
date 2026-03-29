package com.planup.planup.domain.complaint.service;

import com.planup.planup.domain.complaint.dto.ComplaintRequestDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface GoalComplaintService {

    @Transactional
    void reportGoal(Long reporterId, Long goalId, ComplaintRequestDTO request);

    // 신고자가 신고한 goalId 목록 반환 (조회 필터링용)
    List<Long> getReportedGoalIds(Long reporterId);
}
