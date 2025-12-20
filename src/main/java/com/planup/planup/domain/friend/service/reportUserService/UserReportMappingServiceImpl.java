package com.planup.planup.domain.friend.service.reportUserService;

import com.planup.planup.domain.friend.dto.FriendReportRequestDTO;
import com.planup.planup.domain.friend.entity.reportEntity.ReportStatus;
import com.planup.planup.domain.friend.entity.reportEntity.UserReportMapping;
import com.planup.planup.domain.friend.repository.UserReportMappingRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.query.UserQueryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@AllArgsConstructor
@Slf4j
public class UserReportMappingServiceImpl implements UserReportMappingService {

    private final UserQueryService userService;
    private final UserReportMappingRepository userReportMappingRepository;

    @Override
    @Transactional
    public boolean createReportUser(FriendReportRequestDTO request, Long userId) {

        Long friendId= request.getFriendId();
        String reason = request.getReason();
        boolean block = request.isBlock();

        User reporter = userService.getUserByUserId(userId);
        User reported = userService.getUserByUserId(friendId);

        UserReportMapping userReport = UserReportMapping.builder()
                .reporter(reporter)
                .reported(reported)
                .reason(reason)
                .blocked(block)
                .status(ReportStatus.PENDING)
                .build();

        userReportMappingRepository.save(userReport);
        return true;
    }
}
