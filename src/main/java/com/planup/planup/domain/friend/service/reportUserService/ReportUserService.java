package com.planup.planup.domain.friend.service.reportUserService;

import com.planup.planup.domain.friend.dto.FriendReportRequestDTO;
import org.springframework.transaction.annotation.Transactional;

public interface ReportUserService {
    @Transactional
    boolean createReportUser(FriendReportRequestDTO request);
}
