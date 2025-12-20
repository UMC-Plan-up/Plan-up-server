package com.planup.planup.domain.friend.service.policy;

import com.planup.planup.domain.friend.converter.FriendConverter;
import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.goal.repository.UserGoalRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import com.planup.planup.domain.verification.repository.TimerVerificationRepository;
import com.planup.planup.domain.verification.service.TimerVerificationReadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendSummaryAssembler {

    private final UserGoalRepository userGoalRepository;
    private final PhotoVerificationRepository photoVerificationRepository;
    private final TimerVerificationReadService timerVerificationService;
    private final FriendConverter friendConverter;
    private final TimerVerificationRepository timerVerificationRepository;

    public FriendResponseDTO.FriendInfoSummary assemble(User friend) {
        int goalCnt = Math.toIntExact(userGoalRepository.countByUserId(friend.getId()));
        boolean isNewPhotoVerify = existsTodayPhotoVerification(friend);
        LocalTime todayTime = calcTodayTotalTime(friend);
        return friendConverter.toFriendSummary(friend, goalCnt, isNewPhotoVerify, todayTime, friend.getProfileImg());
    }

    private boolean existsTodayPhotoVerification(User user) {
        try {
            return photoVerificationRepository.existsTodayPhotoVerificationByUserId(user.getId());
        } catch (Exception e) {
            log.warn("오늘 사진 인증 조회 실패 user={}", user.getId(), e);
            return false;
        }
    }

    private LocalTime calcTodayTotalTime(User user) {
        long totalSeconds = user.getUserGoals().stream()
                .mapToLong(ug -> {
                    try { return timerVerificationRepository.sumTodayVerificationsByUserGoalId(ug.getId()); }
                    catch (Exception e) { log.warn("타이머 합계 실패 userGoal={}", ug.getId(), e); return 0L; }
                }).sum();
        return LocalTime.ofSecondOfDay(totalSeconds);
    }
}
