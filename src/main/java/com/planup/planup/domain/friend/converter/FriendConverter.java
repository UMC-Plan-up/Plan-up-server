package com.planup.planup.domain.friend.converter;

import com.planup.planup.domain.friend.dto.FriendResponseDTO;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.verification.service.TimerVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class FriendConverter {

    public static FriendResponseDTO.FriendInfoSummary toFriendSummary(User friend) {
        return FriendResponseDTO.FriendInfoSummary
                .builder()
                .id(friend.getId())
                .nickname(friend.getNickname()) 
                .goalCnt(0)
                .isNewPhotoVerify(true)
                .build();

    }

    public static FriendResponseDTO.FriendInfoInChallengeCreate toFriendInfoChallenge(User friend) {
        return FriendResponseDTO.FriendInfoInChallengeCreate
                .builder()
                .id(friend.getId())
                .nickname(friend.getNickname())
                .goalCnt(friend.getUserGoals().size())
                .build();

    }

        public static FriendResponseDTO.FriendSummaryList toFriendSummaryList(List<User> friends) {
        List<FriendResponseDTO.FriendInfoSummary> summeryList = friends.stream().map(FriendConverter::toFriendSummary).collect(Collectors.toList());
        int size = summeryList.size();

        return FriendResponseDTO.FriendSummaryList
                .builder()
                .cnt(size)
                .friendInfoSummaryList(summeryList)
                .build();
    }

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
}
