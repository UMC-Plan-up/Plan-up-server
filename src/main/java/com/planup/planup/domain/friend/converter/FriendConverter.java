package com.planup.planup.domain.friend.converter;

import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.verification.service.TimerVerificationService;
import com.planup.planup.domain.verification.repository.PhotoVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendConverter {

    private final TimerVerificationService timerVerificationService;
    private final PhotoVerificationRepository photoVerificationRepository;

    public FriendResponseDTO.FriendInfoSummary toFriendSummary(User friend) {
        // goalCnt: 실제 사용자의 목표 개수
        int goalCnt = friend.getUserGoals().size();
        
        // todayTime: 오늘 타이머 시간 계산 (모든 목표의 합계)
        LocalTime todayTime = calculateTodayTotalTime(friend);

        // isNewPhotoVerify: 오늘 사진 인증 여부 확인
        boolean isNewPhotoVerify = checkTodayPhotoVerification(friend);
        
        return FriendResponseDTO.FriendInfoSummary
                .builder()
                .id(friend.getId())
                .nickname(friend.getNickname()) 
                .goalCnt(goalCnt)
                .todayTime(todayTime)
                .isNewPhotoVerify(true) // 아직 하드코딩
                .build();
    }

    public FriendResponseDTO.FriendInfoInChallengeCreate toFriendInfoChallenge(User friend) {
        return FriendResponseDTO.FriendInfoInChallengeCreate
                .builder()
                .id(friend.getId())
                .nickname(friend.getNickname())
                .goalCnt(friend.getUserGoals().size())
                .build();
    }

    public FriendResponseDTO.FriendSummaryList toFriendSummaryList(List<User> friends) {
        List<FriendResponseDTO.FriendInfoSummary> summeryList = friends.stream().map(this::toFriendSummary).collect(Collectors.toList());
        int size = summeryList.size();

        return FriendResponseDTO.FriendSummaryList
                .builder()
                .cnt(size)
                .friendInfoSummaryList(summeryList)
                .build();
    }
    
    /**
     * 사용자의 모든 목표에 대한 오늘 타이머 시간을 합계하여 계산
     */
    private LocalTime calculateTodayTotalTime(User user) {
        try {
            // 사용자의 모든 목표에 대해 오늘 타이머 시간을 합계
            long totalSeconds = user.getUserGoals().stream()
                    .mapToLong(userGoal -> {
                        try {
                            LocalTime goalTime = timerVerificationService.getTodayTotalTime(user.getId(), userGoal.getGoal().getId());
                            return goalTime.toSecondOfDay();
                        } catch (Exception e) {
                            log.warn("사용자 {} 목표 {} 타이머 시간 조회 실패: {}", user.getNickname(), userGoal.getGoal().getId(), e.getMessage());
                            return 0L;
                        }
                    })
                    .sum();
            
            return LocalTime.ofSecondOfDay(totalSeconds);
        } catch (Exception e) {
            log.warn("사용자 {} 오늘 타이머 시간 계산 실패: {}", user.getNickname(), e.getMessage());
            return LocalTime.of(0, 0, 0);
        }
    }

    private boolean checkTodayPhotoVerification(User user) {
        try {
            return photoVerificationRepository.existsTodayPhotoVerificationByUserId(user.getId());
        } catch (Exception e) {
            log.warn("사용자 {} 오늘 사진 인증 조회 실패: {}", user.getNickname(), e.getMessage());
            return false;
        }
    }
}
