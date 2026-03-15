package com.planup.planup.domain.friend.service.reportUserService;

import com.planup.planup.domain.friend.dto.FriendReportRequestDTO;
import com.planup.planup.domain.user.enums.SanctionDetailReason;
import org.springframework.transaction.annotation.Transactional;

public interface UserReportMappingService {
    @Transactional
    boolean createReportUser(FriendReportRequestDTO request, Long userId);

    // 댓글/사진 신고 누적으로 인한 시스템 자동 유저 신고
    @Transactional
    void createSystemReportUser(Long reporterId, Long targetUserId, SanctionDetailReason reason);
}
